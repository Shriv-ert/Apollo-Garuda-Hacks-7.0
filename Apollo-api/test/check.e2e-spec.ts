import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication, ValidationPipe } from '@nestjs/common';
import request from 'supertest';
import { App } from 'supertest/types';
import { AppModule } from './../src/app.module';
import { LlmService } from './../src/llm/llm.service';
import { HttpExceptionFilter } from './../src/common/filters/http-exception.filter';
import { ResponseInterceptor } from './../src/common/interceptors/response.interceptor';

describe('CheckController (e2e)', () => {
  let app: INestApplication<App>;
  let userToken: string;

  const mockLlmService = {
    enabled: true,
    extractEntities: jest.fn().mockImplementation(async (buffer: Buffer) => {
      const str = buffer.toString('utf-8');
      if (str.includes('multi')) {
        return ['danakilat.xyz', '08123456789', '9911223344'];
      }
      if (str.includes('single')) {
        return ['08123456789'];
      }
      return [];
    }),
    extractEntitiesFromText: jest.fn().mockImplementation(async (text: string) => {
      if (text.includes('asdfghjkl')) {
        return [];
      }
      if (text.includes('021-1500-888')) {
        return ['021-1500-888'];
      }
      if (text.includes('812 3456 789') || text.includes('08123456789')) {
        if (text.includes('danakilat.xyz')) {
          return ['danakilat.xyz', '08123456789'];
        }
        return ['+62 812 3456 789'];
      }
      if (text.includes('pinjamcepat.xyz')) {
        return ['https://pinjamcepat.xyz/'];
      }
      return [text];
    }),
  };

  beforeAll(async () => {
    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    })
      .overrideProvider(LlmService)
      .useValue(mockLlmService)
      .compile();

    app = moduleFixture.createNestApplication();
    app.setGlobalPrefix('api/v1');
    app.useGlobalPipes(
      new ValidationPipe({
        transform: true,
        whitelist: true,
        forbidNonWhitelisted: true,
      }),
    );
    app.useGlobalFilters(new HttpExceptionFilter());
    app.useGlobalInterceptors(new ResponseInterceptor());

    await app.init();

    // Register user & obtain JWT token
    const regRes = await request(app.getHttpServer())
      .post('/api/v1/auth/register')
      .send({
        email: 'checktester@awam.id',
        full_name: 'Check Tester',
        password: 'password123',
      })
      .expect(201);

    userToken = regRes.body.data.token;
  });

  afterAll(async () => {
    await app.close();
  });

  describe('Journey 3: Text Scan & Check', () => {
    it('3A. Scan via Teks Manual — Entitas Terdaftar OJK (AMAN)', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/check')
        .set('Authorization', `Bearer ${userToken}`)
        .send({ value: '021-1500-888' })
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.message).toBe('Pemeriksaan selesai');
      expect(response.body.data.verdict).toBe('AMAN');
      expect(response.body.data.status).toBe('ojk_verified');
      expect(response.body.data.risk_score).toBe(0);
      expect(response.body.data.confidence_score).toBe(95);
    });

    it('3B. Scan via Teks Manual — Entitas Tidak Terdaftar OJK + Scammer (BAHAYA)', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/check')
        .set('Authorization', `Bearer ${userToken}`)
        .send({ value: '+62 812 3456 789' })
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data.verdict).toBe('BAHAYA PENIPUAN');
      expect(response.body.data.status).toBe('scammer');
      expect(response.body.data.risk_score).toBe(75);
    });

    it('3C. Scan via Teks Manual — Entitas Asing / Belum Dilaporkan (WASPADA)', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/check')
        .set('Authorization', `Bearer ${userToken}`)
        .send({ value: 'https://pinjamcepat.xyz/' })
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data.verdict).toBe('WASPADA');
      expect(response.body.data.status).toBe('unknown');
      expect(response.body.data.risk_score).toBe(45);
      expect(response.body.data.confidence_score).toBe(40);
    });

    it('3C-2. Scan via Teks Narasi / Paste Paragraf Pesan (AI Text Extraction)', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/check')
        .set('Authorization', `Bearer ${userToken}`)
        .send({
          value: 'Tolong waspada penipuan pinjol dari nomor 08123456789 dan website http://danakilat.xyz',
        })
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data.verdict).toBe('BAHAYA PENIPUAN');
      expect(response.body.data.entity_value).toBe('danakilat.xyz');
      expect(response.body.data.risk_score).toBe(100);
    });

    it('8B. Input Entitas Format Tidak Valid', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/check')
        .set('Authorization', `Bearer ${userToken}`)
        .send({ value: 'asdfghjkl' })
        .expect(400);

      expect(response.body.success).toBe(false);
      expect(response.body.message).toContain('Format entitas tidak dikenali');
    });
  });

  describe('Journey 3D & 3E: Image Scan (AI Vision / OCR)', () => {
    it('3D. Scan via Gambar / Screenshot (OCR Single Entity)', async () => {
      const dummyBuffer = Buffer.from('single entity screenshot content');

      const response = await request(app.getHttpServer())
        .post('/api/v1/check/image')
        .set('Authorization', `Bearer ${userToken}`)
        .attach('image', dummyBuffer, 'screenshot.jpg')
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.message).toBe('Pemeriksaan gambar selesai');
      expect(response.body.data.summary.entity_value).toBe('08123456789');
      expect(response.body.data.summary.verdict).toBe('BAHAYA PENIPUAN');
      expect(response.body.data.entities).toHaveLength(1);
    });

    it('3E. Scan via Gambar — Ditemukan Banyak Entitas (Multi Entity Summary)', async () => {
      const dummyBuffer = Buffer.from('multi entity screenshot content');

      const response = await request(app.getHttpServer())
        .post('/api/v1/check/image')
        .set('Authorization', `Bearer ${userToken}`)
        .attach('image', dummyBuffer, 'screenshot.jpg')
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data.summary.entity_value).toBe('danakilat.xyz');
      expect(response.body.data.summary.risk_score).toBe(100);
      expect(response.body.data.entities.length).toBeGreaterThanOrEqual(2);
    });

    it('3D Edge Case: Gambar Blur / Tidak Ada Entitas Terdeteksi', async () => {
      const dummyBuffer = Buffer.from('blur image without entities');

      const response = await request(app.getHttpServer())
        .post('/api/v1/check/image')
        .set('Authorization', `Bearer ${userToken}`)
        .attach('image', dummyBuffer, 'blur.jpg')
        .expect(422);

      expect(response.body.success).toBe(false);
      expect(response.body.message).toContain('Tidak dapat mendeteksi entitas');
    });

    it('8C. Upload Gambar Terlalu Besar (> 10MB)', async () => {
      const largeBuffer = Buffer.alloc(10 * 1024 * 1024 + 100);

      const response = await request(app.getHttpServer())
        .post('/api/v1/check/image')
        .set('Authorization', `Bearer ${userToken}`)
        .attach('image', largeBuffer, 'huge.jpg')
        .expect(413);

      expect(response.body.success).toBe(false);
      expect(response.body.message).toContain('Ukuran file terlalu besar');
    });

    it('LLM Service Disabled (503 Service Unavailable)', async () => {
      mockLlmService.enabled = false;

      const dummyBuffer = Buffer.from('single entity content');

      const response = await request(app.getHttpServer())
        .post('/api/v1/check/image')
        .set('Authorization', `Bearer ${userToken}`)
        .attach('image', dummyBuffer, 'screenshot.jpg')
        .expect(503);

      expect(response.body.success).toBe(false);
      expect(response.body.message).toContain('Fitur pindai gambar belum tersedia');

      mockLlmService.enabled = true;
    });
  });
});
