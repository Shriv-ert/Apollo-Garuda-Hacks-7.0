import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication, ValidationPipe } from '@nestjs/common';
import request from 'supertest';
import { App } from 'supertest/types';
import { AppModule } from './../src/app.module';
import { HttpExceptionFilter } from './../src/common/filters/http-exception.filter';
import { ResponseInterceptor } from './../src/common/interceptors/response.interceptor';
import * as bcrypt from 'bcrypt';
import { PrismaService } from './../src/prisma/prisma.service';

describe('AdminController (e2e)', () => {
  let app: INestApplication<App>;
  let adminToken: string;
  let regularToken: string;
  let testReportId1: string;
  let testReportId2: string;

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

    const prisma = app.get(PrismaService);
    const adminHash = await bcrypt.hash('admin123', 10);
    await prisma.user.upsert({
      where: { email: 'admin@awam.id' },
      update: { passwordHash: adminHash, role: 'admin' },
      create: {
        email: 'admin@awam.id',
        fullName: 'Administrator AWAM',
        passwordHash: adminHash,
        role: 'admin',
      },
    });

    // Ensure victim user doesn't exist from previous runs
    await prisma.report.deleteMany({
      where: { user: { email: 'victim@awam.id' } },
    });
    await prisma.user.deleteMany({
      where: { email: 'victim@awam.id' },
    });

    // 1. Login as Admin
    const adminLoginRes = await request(app.getHttpServer())
      .post('/api/v1/auth/login')
      .send({
        email: 'admin@awam.id',
        password: 'admin123',
      })
      .expect(200);
    adminToken = adminLoginRes.body.data.token;

    // 2. Register regular user
    const regRes = await request(app.getHttpServer())
      .post('/api/v1/auth/register')
      .send({
        email: 'victim@awam.id',
        full_name: 'Victim User',
        password: 'password123',
      })
      .expect(201);
    regularToken = regRes.body.data.token;

    // 3. Submit Report 1
    const repRes1 = await request(app.getHttpServer())
      .post('/api/v1/report')
      .set('Authorization', `Bearer ${regularToken}`)
      .send({
        entity_value: '08555444333',
        entity_type: 'phone',
        category: 'Penipuan Transfer',
        description: 'Mentransfer sejumlah uang tapi barang tidak dikirim.',
        proof_image: 'https://example.com/bukti1.jpg',
      })
      .expect(201);
    testReportId1 = repRes1.body.data.id;

    // 4. Submit Report 2 (Multi-entity report to test intra-report graph edge generation)
    const repRes2 = await request(app.getHttpServer())
      .post('/api/v1/report')
      .set('Authorization', `Bearer ${regularToken}`)
      .send({
        entities: [
          { entity_value: '1122334455', entity_type: 'bank_account' },
          { entity_value: '08555999888', entity_type: 'phone' },
        ],
        category: 'Penipuan Rekening',
        description: 'Nomor rekening penampung dan nomor kontak penipu dari transaksi yang sama.',
        proof_image: 'https://example.com/bukti2.jpg',
      })
      .expect(201);
    testReportId2 = repRes2.body.data.id;
  });

  afterAll(async () => {
    await app.close();
  });

  describe('Journey 7D: Access Control (RBAC)', () => {
    it('7D. Akses Endpoint Admin oleh Pengguna Biasa Ditolak (403 Forbidden)', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/v1/admin/reports')
        .set('Authorization', `Bearer ${regularToken}`)
        .expect(403);

      expect(response.body.success).toBe(false);
      expect(response.body.message).toContain('Akses ditolak');
    });
  });

  describe('Journey 7: Admin Verification Workflow & Dashboard', () => {
    it('7C. Admin Melihat Ringkasan Dashboard Statistik', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/v1/admin/dashboard')
        .set('Authorization', `Bearer ${adminToken}`)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data.total_reports).toBeGreaterThanOrEqual(2);
      expect(response.body.data.by_status).toBeDefined();
    });

    it('7A. Admin Melihat Daftar Semua Laporan', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/v1/admin/reports')
        .set('Authorization', `Bearer ${adminToken}`)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data.items.length).toBeGreaterThanOrEqual(2);
    });

    it('7E. Admin Mengubah Status Laporan Menjadi Reviewing', async () => {
      const response = await request(app.getHttpServer())
        .put(`/api/v1/admin/reports/${testReportId1}/reviewing`)
        .set('Authorization', `Bearer ${adminToken}`)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data.status).toBe('reviewing');
    });

    it('7A & 6C. Admin Verifikasi Laporan 1 (Dampak ke Skor & Graf)', async () => {
      const response = await request(app.getHttpServer())
        .put(`/api/v1/admin/reports/${testReportId1}/verify`)
        .set('Authorization', `Bearer ${adminToken}`)
        .send({
          review_note: 'Bukti transfer valid dan sesuai dengan kronologi.',
        })
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data.status).toBe('verified');
      expect(response.body.data.entities[0].status).toBe('scammer');
      expect(response.body.data.entities[0].report_count).toBe(1);
    });

    it('7A & 6C. Admin Verifikasi Laporan 2 (Otomatis Menghubungkan Graf Edge Intra-Report)', async () => {
      const response = await request(app.getHttpServer())
        .put(`/api/v1/admin/reports/${testReportId2}/verify`)
        .set('Authorization', `Bearer ${adminToken}`)
        .send({
          review_note: 'Rekening dan nomor telepon terbukti terkait dalam satu transaksi penipuan.',
        })
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data.status).toBe('verified');
      expect(response.body.data.entities.length).toBe(2);
    });
  });
});
