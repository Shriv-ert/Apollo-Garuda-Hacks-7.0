import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication, ValidationPipe } from '@nestjs/common';
import request from 'supertest';
import { AppModule } from './../src/app.module';
import { HttpExceptionFilter } from './../src/common/filters/http-exception.filter';
import { ResponseInterceptor } from './../src/common/interceptors/response.interceptor';

import * as bcrypt from 'bcrypt';
import { PrismaService } from './../src/prisma/prisma.service';

describe('GraphController (e2e)', () => {
  let app: INestApplication;
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

    // Ensure admin user exists in DB
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

    // Login as admin or user
    const loginRes = await request(app.getHttpServer())
      .post('/api/v1/auth/login')
      .send({
        email: 'admin@awam.id',
        password: 'admin123',
      })
      .expect(200);

    userToken = loginRes.body.data.token;
  });

  afterAll(async () => {
    await app.close();
  });

  describe('Journey 6: Fraud Network Graph Visualization', () => {
    it('6A. Ambil Data Graf Jaringan Penipuan (Nodes & Edges)', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/v1/graph')
        .set('Authorization', `Bearer ${userToken}`)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data.nodes).toBeDefined();
      expect(Array.isArray(response.body.data.nodes)).toBe(true);
      expect(response.body.data.edges).toBeDefined();
      expect(Array.isArray(response.body.data.edges)).toBe(true);

      // Verify node format
      if (response.body.data.nodes.length > 0) {
        const node = response.body.data.nodes[0];
        expect(node.id).toBeDefined();
        expect(node.value).toBeDefined();
        expect(node.status).toBeDefined();
        expect(node.risk_score).toBeDefined();
      }
    });
  });
});
