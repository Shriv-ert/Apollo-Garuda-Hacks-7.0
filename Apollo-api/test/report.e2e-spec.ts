import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication, ValidationPipe } from '@nestjs/common';
import request from 'supertest';
import { App } from 'supertest/types';
import { AppModule } from './../src/app.module';
import { HttpExceptionFilter } from './../src/common/filters/http-exception.filter';
import { ResponseInterceptor } from './../src/common/interceptors/response.interceptor';

import { PrismaService } from './../src/prisma/prisma.service';

describe('ReportController (e2e)', () => {
  let app: INestApplication<App>;
  let userToken1: string;
  let userToken2: string;
  let createdReportId: string;

  beforeAll(async () => {
    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();

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

    // Clean up any existing test users
    const prisma = app.get(PrismaService);
    await prisma.report.deleteMany({
      where: { user: { email: { in: ['reporter1@awam.id', 'reporter2@awam.id'] } } },
    });
    await prisma.user.deleteMany({
      where: { email: { in: ['reporter1@awam.id', 'reporter2@awam.id'] } },
    });

    // Register User 1
    const regRes1 = await request(app.getHttpServer())
      .post('/api/v1/auth/register')
      .send({
        email: 'reporter1@awam.id',
        full_name: 'Reporter One',
        password: 'password123',
      })
      .expect(201);
    userToken1 = regRes1.body.data.token;

    // Register User 2
    const regRes2 = await request(app.getHttpServer())
      .post('/api/v1/auth/register')
      .send({
        email: 'reporter2@awam.id',
        full_name: 'Reporter Two',
        password: 'password123',
      })
      .expect(201);
    userToken2 = regRes2.body.data.token;
  });

  afterAll(async () => {
    await app.close();
  });

  describe('Journey 4: Submit Report', () => {
    it('4A. Kirim Laporan Lengkap (Single Entity Legacy)', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/report')
        .set('Authorization', `Bearer ${userToken1}`)
        .send({
          entity_value: '08999888777',
          entity_type: 'phone',
          category: 'Pinjol Ilegal',
          description: 'Ditagih dengan ancaman padahal tidak pernah meminjam uang.',
          proof_image: 'https://example.com/proof1.jpg',
        })
        .expect(201);

      expect(response.body.success).toBe(true);
      expect(response.body.message).toContain('Laporan berhasil dikirim');
      expect(response.body.data.status).toBe('pending');
      expect(response.body.data.entities.length).toBe(1);
      expect(response.body.data.entities[0].value).toBe('08999888777');

      createdReportId = response.body.data.id;
    });

    it('4B. Kirim Laporan dengan Banyak Entitas Sekaligus (Multi Entity)', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/report')
        .set('Authorization', `Bearer ${userToken1}`)
        .send({
          entities: [
            { entity_value: '081122334455', entity_type: 'phone' },
            { entity_value: 'BCA 9988776655', entity_type: 'bank_account' },
            { entity_value: 'http://penipu.xyz', entity_type: 'url' },
          ],
          category: 'Penipuan Online',
          description: 'Pelaku meminta transfer ke rekening BCA setelah kontak via WA dan kirim link phising.',
          proof_image: 'https://example.com/proof_multi.jpg',
        })
        .expect(201);

      expect(response.body.success).toBe(true);
      expect(response.body.data.status).toBe('pending');
      expect(response.body.data.entities.length).toBe(3);
    });

    it('4D Error: Kirim Laporan Tanpa Bukti Tangkapan Layar (Bad Request)', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/report')
        .set('Authorization', `Bearer ${userToken1}`)
        .send({
          entity_value: '08999888777',
          category: 'Pinjol Ilegal',
          description: 'Ditagih dengan ancaman tanpa bukti',
        })
        .expect(400);

      expect(response.body.success).toBe(false);
    });

    it('4E Error: Kirim Laporan Tanpa Kategori (Bad Request)', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/report')
        .set('Authorization', `Bearer ${userToken1}`)
        .send({
          entity_value: '08999888777',
          description: 'Ditagih dengan ancaman tanpa bukti',
          proof_image: 'https://example.com/proof.jpg',
        })
        .expect(400);

      expect(response.body.success).toBe(false);
    });
  });

  describe('Journey 5: Report History & Detail', () => {
    it('5A. Lihat Riwayat Laporan Milik Sendiri', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/v1/reports/history')
        .set('Authorization', `Bearer ${userToken1}`)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data.items.length).toBeGreaterThanOrEqual(1);
      expect(response.body.data.pagination.total).toBeGreaterThanOrEqual(1);
    });

    it('5C. Filter Riwayat Laporan Berdasarkan Status', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/v1/reports/history?status=pending')
        .set('Authorization', `Bearer ${userToken1}`)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data.items.every((item: any) => item.status === 'pending')).toBe(true);
    });

    it('5D. Lihat Detail Laporan Milik Sendiri', async () => {
      const response = await request(app.getHttpServer())
        .get(`/api/v1/reports/${createdReportId}`)
        .set('Authorization', `Bearer ${userToken1}`)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data.id).toBe(createdReportId);
    });

    it('8F Error: Pengguna Lain Membuka Detail Laporan Orang Lain (Forbidden 403)', async () => {
      const response = await request(app.getHttpServer())
        .get(`/api/v1/reports/${createdReportId}`)
        .set('Authorization', `Bearer ${userToken2}`)
        .expect(403);

      expect(response.body.success).toBe(false);
      expect(response.body.message).toContain('tidak memiliki akses');
    });
  });
});
