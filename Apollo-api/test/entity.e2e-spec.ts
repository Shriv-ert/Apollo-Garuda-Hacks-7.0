import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication, ValidationPipe } from '@nestjs/common';
import request from 'supertest';
import { App } from 'supertest/types';
import { AppModule } from './../src/app.module';
import { HttpExceptionFilter } from './../src/common/filters/http-exception.filter';
import { ResponseInterceptor } from './../src/common/interceptors/response.interceptor';

describe('EntityController (e2e)', () => {
  let app: INestApplication<App>;
  let userToken: string;

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

    // Register test user & obtain JWT token
    const regRes = await request(app.getHttpServer())
      .post('/api/v1/auth/register')
      .send({
        email: 'entitytester@awam.id',
        full_name: 'Entity Tester',
        password: 'password123',
      })
      .expect(201);

    userToken = regRes.body.data.token;
  });

  afterAll(async () => {
    await app.close();
  });

  describe('GET /api/v1/entities', () => {
    it('Error: Akses tanpa token (401 Unauthorized)', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/v1/entities')
        .expect(401);

      expect(response.body.success).toBe(false);
    });

    it('Sukses: Mengambil semua entitas (Tanpa Filter)', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/v1/entities')
        .set('Authorization', `Bearer ${userToken}`)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.message).toContain('berhasil diambil');
      expect(response.body.data.items.length).toBeGreaterThanOrEqual(1);
      expect(response.body.data.pagination).toBeDefined();
    });

    it('Sukses: Filter entitas berdasarkan tipe "phone"', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/v1/entities?type=phone')
        .set('Authorization', `Bearer ${userToken}`)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(
        response.body.data.items.every((item: any) => item.type === 'phone'),
      ).toBe(true);
    });

    it('Sukses: Filter entitas berdasarkan tipe "bank_account"', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/v1/entities?type=bank_account')
        .set('Authorization', `Bearer ${userToken}`)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(
        response.body.data.items.every(
          (item: any) => item.type === 'bank_account',
        ),
      ).toBe(true);
    });

    it('Sukses: Search entitas dengan query q="danakilat"', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/v1/entities?q=danakilat')
        .set('Authorization', `Bearer ${userToken}`)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(
        response.body.data.items.some((item: any) =>
          item.value.includes('danakilat'),
        ),
      ).toBe(true);
    });
  });
});
