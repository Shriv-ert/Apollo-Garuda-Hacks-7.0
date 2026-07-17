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
    { type: 'url', value: 'p2p.danamas.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'amartha.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'dompetkilat.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'myboost.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'tokomodal.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'modalku.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'pendanaan.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'kreditpintar.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'maucash.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'finmas.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'klika2c.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'akseleran.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'ammana.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'pinjamango.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'koinp2p.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'pohondana.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'mekar.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'adakami.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'estakapital.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'kreditpro.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'fintag.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'rupiahcepat.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'crowdo.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'indodana.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'julo.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'pinjamwinwin.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'danarupiah.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'ovofinansial.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'pinjammodal.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'alamisharia.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'awantunai.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'danakini.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'singa.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'danamerdeka.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'easycash.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'pinjamyuk.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'finplus.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'uangme.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'pinjamduit.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'danasyariah.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'batumbu.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'cashcepat.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'klikUMKM.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'kreditplusteknologi.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'cicil.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'lumbungdana.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: '360kredi.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'kredinesia.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'pintek.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'modalrakyat.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'solusi-ku.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'cairin.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'trustiq.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'klikkami.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'duhasyariah.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'invoila.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'sanders.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'danabagus.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'ukuindo.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'kredito.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'adapundi.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'lenteradana.co.id/lender', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'modalnasional.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'komunal.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'restock.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'ringan.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'avantee.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'gradana.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'danacita.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'ikimodal.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'ivoji.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'indofund.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'igrow.asia', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'danai.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'minjem.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'lahansikam.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'qazwa.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'kredifazz.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'doeku.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'aktivaku.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'danain.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'indosaku.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'edufund.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'gandengtangan.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'papitupisyariah.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'bantusaku.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'danabijak.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'adamodal.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'samakita.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'kawancicil.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'crowde.co', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'klikcair.com', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'ethis.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'samir.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'uatas.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'asetku.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'findaya.co.id', status: 'ojk_verified', riskScore: 0, confidenceScore: 95, reportCount: 0 }
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
