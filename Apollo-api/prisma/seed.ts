import { PrismaClient } from '@prisma/client';
import * as bcrypt from 'bcrypt';

const prisma = new PrismaClient();

async function main() {
  console.log('Seeding database...');

  // 1. Seed Admin Account
  const adminPasswordHash = await bcrypt.hash('admin123', 10);
  const admin = await prisma.user.upsert({
    where: { email: 'admin@awam.id' },
    update: {},
    create: {
      email: 'admin@awam.id',
      fullName: 'Administrator AWAM',
      passwordHash: adminPasswordHash,
      role: 'admin',
    },
  });
  console.log(`Admin user seeded: ${admin.email}`);

  // 2. Seed OJK Verified Entities
  const ojkVerifiedEntities = [
    { type: 'phone', value: '0211500888', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'phone', value: '14017', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'phone', value: '14000', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'bca.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'kreditpintar.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'akulaku.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
  ];

  for (const e of ojkVerifiedEntities) {
    await prisma.entity.upsert({
      where: { value: e.value },
      update: e,
      create: e,
    });
  }

  // 3. Seed Scammer Entities
  const scammerEntities = [
    { type: 'url', value: 'danakilat.xyz', status: 'scammer', riskScore: 100, confidenceScore: 100, reportCount: 7 },
    { type: 'phone', value: '08123456789', status: 'scammer', riskScore: 75, confidenceScore: 90, reportCount: 5 },
    { type: 'bank_account', value: '8720123456', status: 'scammer', riskScore: 45, confidenceScore: 70, reportCount: 3 },
  ];

  for (const e of scammerEntities) {
    await prisma.entity.upsert({
      where: { value: e.value },
      update: e,
      create: e,
    });
  }

  // 4. Seed OJK Illegal Entities
  const ojkIllegalEntities = [
    { type: 'url', value: 'pinjamancepat.id', status: 'ojk_illegal', riskScore: 95, confidenceScore: 95, reportCount: 0 },
  ];

  for (const e of ojkIllegalEntities) {
    await prisma.entity.upsert({
      where: { value: e.value },
      update: e,
      create: e,
    });
  }

  // 5. Seed Entity Relations (Graph Edges)
  const phoneScammer = await prisma.entity.findUnique({ where: { value: '08123456789' } });
  const bankScammer = await prisma.entity.findUnique({ where: { value: '8720123456' } });

  if (phoneScammer && bankScammer) {
    const existingRelation = await prisma.entityRelation.findFirst({
      where: { sourceId: phoneScammer.id, targetId: bankScammer.id },
    });
    if (!existingRelation) {
      await prisma.entityRelation.create({
        data: {
          sourceId: phoneScammer.id,
          targetId: bankScammer.id,
          relationType: 'menerima_transfer_ke',
        },
      });
    }
  }

  console.log('Database seeding completed successfully!');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
