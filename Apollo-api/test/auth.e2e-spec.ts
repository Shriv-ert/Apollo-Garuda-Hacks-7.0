import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication, ValidationPipe } from '@nestjs/common';
import request from 'supertest';
import { App } from 'supertest/types';
import { AppModule } from './../src/app.module';
import { PrismaService } from './../src/prisma/prisma.service';
import { HttpExceptionFilter } from './../src/common/filters/http-exception.filter';
import { ResponseInterceptor } from './../src/common/interceptors/response.interceptor';

describe('AuthController (e2e)', () => {
  let app: INestApplication<App>;
  let prismaService: PrismaService;

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

    prismaService = app.get(PrismaService);
  });

  beforeEach(async () => {
    // Clear reports and user tables before each test for clean state
    await prismaService.report.deleteMany({});
    await prismaService.user.deleteMany({});
  });

  afterAll(async () => {
    await prismaService.report.deleteMany({});
    await prismaService.user.deleteMany({});
    await app.close();
  });

  const testUser = {
    email: 'user@awam.id',
    full_name: 'Budi Santoso',
    password: 'password123',
  };

  describe('Journey 1: Registrasi Akun Baru', () => {
    it('1A. Registrasi Berhasil (Happy Path)', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/auth/register')
        .send(testUser)
        .expect(201);

      expect(response.body.success).toBe(true);
      expect(response.body.message).toBe('Registrasi berhasil');
      expect(response.body.data.token).toBeDefined();
      expect(response.body.data.user.email).toBe(testUser.email);
      expect(response.body.data.user.full_name).toBe(testUser.full_name);
      expect(response.body.data.user.role).toBe('user');
      expect(response.body.data.user.id).toBeDefined();
    });

    it('1B. Registrasi Gagal — Email Sudah Terdaftar', async () => {
      // Register first time
      await request(app.getHttpServer())
        .post('/api/v1/auth/register')
        .send(testUser)
        .expect(201);

      // Register second time with same email
      const response = await request(app.getHttpServer())
        .post('/api/v1/auth/register')
        .send(testUser)
        .expect(409);

      expect(response.body.success).toBe(false);
      expect(response.body.message).toBe('Email sudah terdaftar');
    });

    it('1C. Registrasi Gagal — Password Terlalu Lemah', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/auth/register')
        .send({
          ...testUser,
          password: '123',
        })
        .expect(400);

      expect(response.body.success).toBe(false);
      expect(response.body.message).toContain('Password minimal 8 karakter');
    });

    it('1D. Registrasi Gagal — Nama Lengkap Kosong', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/auth/register')
        .send({
          email: 'user@awam.id',
          password: 'password123',
          full_name: '',
        })
        .expect(400);

      expect(response.body.success).toBe(false);
      expect(response.body.errors).toContain('Nama lengkap wajib diisi');
    });
  });

  describe('Journey 2: Login', () => {
    beforeEach(async () => {
      // Pre-register user before each login test
      await request(app.getHttpServer())
        .post('/api/v1/auth/register')
        .send(testUser)
        .expect(201);
    });

    it('2A. Login Berhasil (Happy Path)', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/auth/login')
        .send({
          email: testUser.email,
          password: testUser.password,
        })
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.message).toBe('Login berhasil');
      expect(response.body.data.token).toBeDefined();
      expect(response.body.data.user.email).toBe(testUser.email);
      expect(response.body.data.user.full_name).toBe(testUser.full_name);
    });

    it('2B. Login Gagal — Email Tidak Terdaftar', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/auth/login')
        .send({
          email: 'unknown@awam.id',
          password: testUser.password,
        })
        .expect(401);

      expect(response.body.success).toBe(false);
      expect(response.body.message).toBe('Email atau password salah');
    });

    it('2C. Login Gagal — Password Salah', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/v1/auth/login')
        .send({
          email: testUser.email,
          password: 'wrongpassword',
        })
        .expect(401);

      expect(response.body.success).toBe(false);
      expect(response.body.message).toBe('Email atau password salah');
    });

    it('2D. Akses Fitur dengan Token Kedaluwarsa/Invalid', async () => {
      // Test without header
      let response = await request(app.getHttpServer())
        .get('/api/v1/protected')
        .expect(401);

      expect(response.body.success).toBe(false);
      expect(response.body.message).toBe('Token tidak valid atau sudah kedaluwarsa');

      // Test with invalid token
      response = await request(app.getHttpServer())
        .get('/api/v1/protected')
        .set('Authorization', 'Bearer invalid-token-value')
        .expect(401);

      expect(response.body.success).toBe(false);
      expect(response.body.message).toBe('Token tidak valid atau sudah kedaluwarsa');
    });

    it('2D Happy Path. Akses Fitur dengan Token Valid', async () => {
      const loginRes = await request(app.getHttpServer())
        .post('/api/v1/auth/login')
        .send({
          email: testUser.email,
          password: testUser.password,
        })
        .expect(200);

      const token = loginRes.body.data.token;

      const response = await request(app.getHttpServer())
        .get('/api/v1/protected')
        .set('Authorization', `Bearer ${token}`)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.message).toBe('Access granted');
      expect(response.body.data.user.email).toBe(testUser.email);
    });
  });
});
