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

  // 3. Seed Demo User Accounts (pelapor)
  const userPasswordHash = await bcrypt.hash('user123', 10);
  const demoUsers = [
    { email: 'budi.santoso@demo.id', fullName: 'Budi Santoso', passwordHash: userPasswordHash, role: 'user' },
    { email: 'siti.rahayu@demo.id', fullName: 'Siti Rahayu', passwordHash: userPasswordHash, role: 'user' },
    { email: 'agus.prabowo@demo.id', fullName: 'Agus Prabowo', passwordHash: userPasswordHash, role: 'user' },
    { email: 'dewi.lestari@demo.id', fullName: 'Dewi Lestari', passwordHash: userPasswordHash, role: 'user' },
    { email: 'rizky.firmansyah@demo.id', fullName: 'Rizky Firmansyah', passwordHash: userPasswordHash, role: 'user' },
  ];

  const createdUsers: { id: number; email: string }[] = [admin];
  for (const u of demoUsers) {
    const user = await prisma.user.upsert({
      where: { email: u.email },
      update: {},
      create: u,
    });
    createdUsers.push(user);
  }
  console.log(`Demo users seeded: ${demoUsers.length} users`);

  // 4. Seed Scammer Entities — Nomor Telepon Fiktif (SEMUA DATA INI RANDOM & TIDAK MERUJUK KE NOMOR ASLI)
  const scammerPhones = [
    // Penipu investasi bodong
    { type: 'phone', value: '081234567890', status: 'scammer', riskScore: 95, confidenceScore: 98, reportCount: 23 },
    { type: 'phone', value: '082198765432', status: 'scammer', riskScore: 90, confidenceScore: 95, reportCount: 18 },
    { type: 'phone', value: '085377889900', status: 'scammer', riskScore: 88, confidenceScore: 92, reportCount: 15 },
    { type: 'phone', value: '087711223344', status: 'scammer', riskScore: 85, confidenceScore: 90, reportCount: 12 },
    { type: 'phone', value: '081355667788', status: 'scammer', riskScore: 82, confidenceScore: 88, reportCount: 10 },
    // Penipu pinjol ilegal
    { type: 'phone', value: '089922334455', status: 'scammer', riskScore: 92, confidenceScore: 96, reportCount: 20 },
    { type: 'phone', value: '081288990011', status: 'scammer', riskScore: 78, confidenceScore: 85, reportCount: 8 },
    { type: 'phone', value: '085644332211', status: 'scammer', riskScore: 75, confidenceScore: 82, reportCount: 7 },
    { type: 'phone', value: '082111445566', status: 'scammer', riskScore: 70, confidenceScore: 80, reportCount: 6 },
    { type: 'phone', value: '087899001122', status: 'scammer', riskScore: 68, confidenceScore: 78, reportCount: 5 },
    // Penipu jual-beli online
    { type: 'phone', value: '081377665544', status: 'scammer', riskScore: 80, confidenceScore: 87, reportCount: 11 },
    { type: 'phone', value: '085299887766', status: 'scammer', riskScore: 77, confidenceScore: 84, reportCount: 9 },
    { type: 'phone', value: '082344556677', status: 'scammer', riskScore: 72, confidenceScore: 81, reportCount: 7 },
    { type: 'phone', value: '089911223300', status: 'scammer', riskScore: 65, confidenceScore: 75, reportCount: 4 },
    { type: 'phone', value: '081200998877', status: 'scammer', riskScore: 60, confidenceScore: 72, reportCount: 3 },
    // Penipu undian/hadiah palsu
    { type: 'phone', value: '085633221100', status: 'scammer', riskScore: 98, confidenceScore: 99, reportCount: 30 },
    { type: 'phone', value: '087744558899', status: 'scammer', riskScore: 93, confidenceScore: 97, reportCount: 25 },
    { type: 'phone', value: '082155667700', status: 'scammer', riskScore: 87, confidenceScore: 91, reportCount: 14 },
    // Phishing / social engineering
    { type: 'phone', value: '081399887711', status: 'scammer', riskScore: 91, confidenceScore: 94, reportCount: 19 },
    { type: 'phone', value: '089900112233', status: 'scammer', riskScore: 86, confidenceScore: 90, reportCount: 13 },
    { type: 'phone', value: '085244556600', status: 'scammer', riskScore: 79, confidenceScore: 85, reportCount: 9 },
    // Penipuan berkedok customer service
    { type: 'phone', value: '082177889955', status: 'scammer', riskScore: 94, confidenceScore: 97, reportCount: 22 },
    { type: 'phone', value: '081266778800', status: 'scammer', riskScore: 89, confidenceScore: 93, reportCount: 16 },
    { type: 'phone', value: '087733445511', status: 'scammer', riskScore: 83, confidenceScore: 89, reportCount: 11 },
    // Penipuan lowongan kerja palsu
    { type: 'phone', value: '085600112299', status: 'scammer', riskScore: 76, confidenceScore: 83, reportCount: 8 },
    { type: 'phone', value: '089955443322', status: 'scammer', riskScore: 71, confidenceScore: 79, reportCount: 6 },
    // Penipuan romance scam
    { type: 'phone', value: '081344556611', status: 'scammer', riskScore: 84, confidenceScore: 90, reportCount: 12 },
    { type: 'phone', value: '082100334455', status: 'scammer', riskScore: 73, confidenceScore: 80, reportCount: 7 },
    // Nomor belum terkonfirmasi (status unknown, tapi sudah ada laporan)
    { type: 'phone', value: '085611009988', status: 'unknown', riskScore: 40, confidenceScore: 55, reportCount: 2 },
    { type: 'phone', value: '087700889977', status: 'unknown', riskScore: 35, confidenceScore: 50, reportCount: 1 },
  ];

  // 5. Seed Scammer Bank Accounts — Rekening Fiktif
  const scammerBankAccounts = [
    { type: 'bank_account', value: 'BCA-8720199001', status: 'scammer', riskScore: 95, confidenceScore: 97, reportCount: 15 },
    { type: 'bank_account', value: 'BRI-003301445566', status: 'scammer', riskScore: 90, confidenceScore: 94, reportCount: 12 },
    { type: 'bank_account', value: 'MANDIRI-1560078899001', status: 'scammer', riskScore: 88, confidenceScore: 92, reportCount: 10 },
    { type: 'bank_account', value: 'BNI-0441223344', status: 'scammer', riskScore: 82, confidenceScore: 87, reportCount: 8 },
    { type: 'bank_account', value: 'BSI-7210556677', status: 'scammer', riskScore: 78, confidenceScore: 84, reportCount: 6 },
    { type: 'bank_account', value: 'DANA-081234567890', status: 'scammer', riskScore: 75, confidenceScore: 82, reportCount: 5 },
    { type: 'bank_account', value: 'OVO-082198765432', status: 'scammer', riskScore: 70, confidenceScore: 78, reportCount: 4 },
    { type: 'bank_account', value: 'GOPAY-085377889900', status: 'scammer', riskScore: 65, confidenceScore: 74, reportCount: 3 },
    { type: 'bank_account', value: 'CIMB-7600112233', status: 'scammer', riskScore: 60, confidenceScore: 70, reportCount: 2 },
    { type: 'bank_account', value: 'PERMATA-5510334455', status: 'unknown', riskScore: 30, confidenceScore: 45, reportCount: 1 },
  ];

  // 6. Seed Scammer URLs — URL/Domain Fiktif Penipu
  const scammerUrls = [
    { type: 'url', value: 'danakilat.xyz', status: 'scammer', riskScore: 100, confidenceScore: 100, reportCount: 35 },
    { type: 'url', value: 'pinjaman-kilat88.com', status: 'scammer', riskScore: 97, confidenceScore: 99, reportCount: 28 },
    { type: 'url', value: 'investasi-profit99.site', status: 'scammer', riskScore: 95, confidenceScore: 98, reportCount: 22 },
    { type: 'url', value: 'undian-telkomsel.top', status: 'scammer', riskScore: 93, confidenceScore: 97, reportCount: 19 },
    { type: 'url', value: 'bca-keamanan-update.click', status: 'scammer', riskScore: 98, confidenceScore: 99, reportCount: 31 },
    { type: 'url', value: 'shopee-hadiah-undian.xyz', status: 'scammer', riskScore: 92, confidenceScore: 96, reportCount: 17 },
    { type: 'url', value: 'tokopedia-promo-spesial.site', status: 'scammer', riskScore: 90, confidenceScore: 95, reportCount: 14 },
    { type: 'url', value: 'lowongan-kerja-bumn2026.com', status: 'scammer', riskScore: 88, confidenceScore: 93, reportCount: 11 },
    { type: 'url', value: 'crypto-profit-guaranteed.id', status: 'scammer', riskScore: 96, confidenceScore: 98, reportCount: 26 },
    { type: 'url', value: 'whatsapp-verify-id.online', status: 'scammer', riskScore: 91, confidenceScore: 95, reportCount: 16 },
  ];

  // 7. Seed Scammer Emails — Email Fiktif Penipu
  const scammerEmails = [
    { type: 'email', value: 'cs.bca.secure@gmail.com', status: 'scammer', riskScore: 96, confidenceScore: 98, reportCount: 20 },
    { type: 'email', value: 'hadiah.undian.telkomsel@yahoo.com', status: 'scammer', riskScore: 94, confidenceScore: 97, reportCount: 18 },
    { type: 'email', value: 'invest.profit.cepat@outlook.com', status: 'scammer', riskScore: 90, confidenceScore: 94, reportCount: 13 },
    { type: 'email', value: 'hrd.lowongan.bumn@gmail.com', status: 'scammer', riskScore: 87, confidenceScore: 91, reportCount: 10 },
    { type: 'email', value: 'admin.pinjaman.online88@yahoo.com', status: 'scammer', riskScore: 83, confidenceScore: 88, reportCount: 8 },
  ];

  // Seed semua entity scammer
  const allScammerEntities = [
    ...scammerPhones,
    ...scammerBankAccounts,
    ...scammerUrls,
    ...scammerEmails,
  ];

  for (const e of allScammerEntities) {
    await prisma.entity.upsert({
      where: { value: e.value },
      update: e,
      create: e,
    });
  }
  console.log(`Scammer entities seeded: ${allScammerEntities.length} entities`);

  // 8. Seed OJK Illegal Entities
  const ojkIllegalEntities = [
    { type: 'url', value: 'pinjamancepat.id', status: 'ojk_illegal', riskScore: 95, confidenceScore: 95, reportCount: 0 },
    { type: 'url', value: 'duitcepat-online.com', status: 'ojk_illegal', riskScore: 92, confidenceScore: 93, reportCount: 3 },
    { type: 'url', value: 'pinjamanmudah.xyz', status: 'ojk_illegal', riskScore: 90, confidenceScore: 91, reportCount: 2 },
    { type: 'url', value: 'kasbon-digital.id', status: 'ojk_illegal', riskScore: 88, confidenceScore: 90, reportCount: 1 },
    { type: 'url', value: 'tunai-kilat.site', status: 'ojk_illegal', riskScore: 93, confidenceScore: 94, reportCount: 5 },
  ];

  for (const e of ojkIllegalEntities) {
    await prisma.entity.upsert({
      where: { value: e.value },
      update: e,
      create: e,
    });
  }
  console.log(`OJK illegal entities seeded: ${ojkIllegalEntities.length} entities`);

  // 9. Seed Sample Reports — Laporan demo dari user
  const reportCategories = [
    'Penipuan Investasi',
    'Pinjaman Online Ilegal',
    'Penipuan Jual-Beli Online',
    'Undian/Hadiah Palsu',
    'Phishing',
    'Penipuan Berkedok CS',
    'Lowongan Kerja Palsu',
    'Romance Scam',
  ];

  const reportDescriptions: Record<string, string[]> = {
    'Penipuan Investasi': [
      'Ditawari investasi dengan return 30% per bulan, setelah transfer uang tidak bisa dihubungi',
      'Awalnya untung kecil-kecil, lalu diminta top up besar dan uang tidak bisa ditarik',
      'Mengaku dari perusahaan sekuritas terdaftar, ternyata palsu. Uang Rp 50 juta hilang',
    ],
    'Pinjaman Online Ilegal': [
      'Aplikasi pinjaman meminta akses semua kontak, lalu meneror dan mempermalukan di depan keluarga',
      'Bunga pinjaman 1% per hari, tidak sesuai yang dijanjikan. Total tagihan membengkak 5x lipat',
      'Tidak pernah mengajukan pinjaman tapi tiba-tiba dapat tagihan dan ancaman',
    ],
    'Penipuan Jual-Beli Online': [
      'Beli HP di marketplace, barang yang datang berupa sabun colek. Penjual sudah tidak aktif',
      'Transfer untuk beli tiket konser, ternyata tiket palsu dan nomor penjual sudah tidak aktif',
      'Jual barang online, pembeli kirim bukti transfer palsu. Barang sudah terlanjur dikirim',
    ],
    'Undian/Hadiah Palsu': [
      'Dapat SMS menang undian Rp 100 juta, diminta transfer pajak hadiah Rp 5 juta terlebih dahulu',
      'Telepon mengaku dari provider, bilang menang mobil. Diminta kirim biaya administrasi',
      'WhatsApp dari nomor asing bilang dapat voucher Rp 10 juta, link mengarah ke situs phishing',
    ],
    'Phishing': [
      'Dapat email minta verifikasi akun bank, setelah isi form saldo rekening terkuras habis',
      'Link palsu mirip situs e-commerce, setelah login akun diambil alih dan dipakai belanja',
      'SMS berisi link tracking paket, ternyata instal malware yang mencuri data banking',
    ],
    'Penipuan Berkedok CS': [
      'Ditelepon mengaku CS bank, diminta sebutkan OTP. Saldo tabungan Rp 20 juta hilang',
      'Chat di IG mengaku CS e-wallet, minta screenshot kode QR. Saldo terkuras',
      'WA mengaku CS marketplace, suruh klik link refund. Malah saldo yang ditarik',
    ],
    'Lowongan Kerja Palsu': [
      'Lowongan kerja di WA, diminta bayar biaya pendaftaran Rp 500 ribu. Setelah bayar tidak ada kabar',
      'Diterima kerja WFH data entry, diminta beli laptop khusus dari link mereka. Barang tidak dikirim',
      'Mengaku HRD perusahaan besar, minta transfer untuk seragam dan pelatihan. Ternyata penipuan',
    ],
    'Romance Scam': [
      'Kenalan di dating app, setelah 3 bulan minta pinjam uang Rp 30 juta untuk operasi. Hilang kontak',
      'Mengaku tentara AS, kirim foto dan chat mesra, lalu minta transfer untuk biaya pulang ke Indonesia',
    ],
  };

  // Ambil semua entities yang berstatus scammer untuk dikaitkan dengan reports
  const scammerEntitiesInDb = await prisma.entity.findMany({
    where: { status: 'scammer' },
  });

  if (scammerEntitiesInDb.length > 0 && createdUsers.length > 1) {
    let reportIndex = 0;
    for (const entity of scammerEntitiesInDb.slice(0, 30)) {
      // Pilih kategori berdasarkan tipe entity
      let category: string;
      if (entity.type === 'phone') {
        const phoneCats = ['Penipuan Investasi', 'Pinjaman Online Ilegal', 'Undian/Hadiah Palsu', 'Penipuan Berkedok CS', 'Romance Scam'];
        category = phoneCats[reportIndex % phoneCats.length];
      } else if (entity.type === 'bank_account') {
        const bankCats = ['Penipuan Investasi', 'Penipuan Jual-Beli Online', 'Pinjaman Online Ilegal'];
        category = bankCats[reportIndex % bankCats.length];
      } else if (entity.type === 'url') {
        const urlCats = ['Phishing', 'Pinjaman Online Ilegal', 'Penipuan Investasi', 'Lowongan Kerja Palsu'];
        category = urlCats[reportIndex % urlCats.length];
      } else {
        const emailCats = ['Phishing', 'Undian/Hadiah Palsu', 'Penipuan Berkedok CS'];
        category = emailCats[reportIndex % emailCats.length];
      }

      const descriptions = reportDescriptions[category] || ['Melaporkan penipuan dari nomor/entitas ini'];
      const description = descriptions[reportIndex % descriptions.length];

      // Pilih random user (bukan admin)
      const reporterUser = createdUsers[1 + (reportIndex % (createdUsers.length - 1))];

      // Cek apakah report sudah ada
      const existingReport = await prisma.report.findFirst({
        where: { userId: reporterUser.id, entityId: entity.id },
      });

      if (!existingReport) {
        const statuses = ['pending', 'reviewing', 'verified', 'verified', 'verified'];
        const reportStatus = statuses[reportIndex % statuses.length];

        await prisma.report.create({
          data: {
            userId: reporterUser.id,
            entityId: entity.id,
            category,
            description,
            proofImage: `https://placehold.co/600x400?text=Bukti+${reportIndex + 1}`,
            status: reportStatus,
            reviewedById: reportStatus === 'verified' ? admin.id : null,
            reviewNote: reportStatus === 'verified' ? 'Laporan terverifikasi oleh admin setelah pengecekan bukti.' : null,
            reviewedAt: reportStatus === 'verified' ? new Date() : null,
          },
        });
      }
      reportIndex++;
    }
    console.log(`Sample reports seeded: ${Math.min(30, scammerEntitiesInDb.length)} reports`);
  }

  // 10. Seed Entity Relations (Jaringan Penipuan / Scam Network Graph)
  // Buat koneksi antar entitas scammer untuk menunjukkan jaringan penipuan yang saling terkait
  const relationPairs = [
    // Nomor telepon → rekening bank (menerima transfer)
    { sourceValue: '081234567890', targetValue: 'BCA-8720199001', relationType: 'menerima_transfer_ke' },
    { sourceValue: '082198765432', targetValue: 'BRI-003301445566', relationType: 'menerima_transfer_ke' },
    { sourceValue: '085377889900', targetValue: 'MANDIRI-1560078899001', relationType: 'menerima_transfer_ke' },
    { sourceValue: '087711223344', targetValue: 'BNI-0441223344', relationType: 'menerima_transfer_ke' },
    { sourceValue: '081355667788', targetValue: 'BSI-7210556677', relationType: 'menerima_transfer_ke' },
    { sourceValue: '089922334455', targetValue: 'DANA-081234567890', relationType: 'menerima_transfer_ke' },
    { sourceValue: '081288990011', targetValue: 'OVO-082198765432', relationType: 'menerima_transfer_ke' },
    { sourceValue: '085644332211', targetValue: 'GOPAY-085377889900', relationType: 'menerima_transfer_ke' },
    // Nomor telepon → URL (mempromosikan situs scam)
    { sourceValue: '081234567890', targetValue: 'investasi-profit99.site', relationType: 'mempromosikan' },
    { sourceValue: '085633221100', targetValue: 'undian-telkomsel.top', relationType: 'mempromosikan' },
    { sourceValue: '087744558899', targetValue: 'shopee-hadiah-undian.xyz', relationType: 'mempromosikan' },
    { sourceValue: '082177889955', targetValue: 'bca-keamanan-update.click', relationType: 'mempromosikan' },
    { sourceValue: '081399887711', targetValue: 'whatsapp-verify-id.online', relationType: 'mempromosikan' },
    { sourceValue: '085600112299', targetValue: 'lowongan-kerja-bumn2026.com', relationType: 'mempromosikan' },
    // Rekening bank → rekening bank (transfer antar rekening scammer)
    { sourceValue: 'BCA-8720199001', targetValue: 'DANA-081234567890', relationType: 'transfer_ke' },
    { sourceValue: 'BRI-003301445566', targetValue: 'OVO-082198765432', relationType: 'transfer_ke' },
    { sourceValue: 'MANDIRI-1560078899001', targetValue: 'GOPAY-085377889900', relationType: 'transfer_ke' },
    // Email → URL (email mengarahkan ke situs scam)
    { sourceValue: 'cs.bca.secure@gmail.com', targetValue: 'bca-keamanan-update.click', relationType: 'mengarahkan_ke' },
    { sourceValue: 'hadiah.undian.telkomsel@yahoo.com', targetValue: 'undian-telkomsel.top', relationType: 'mengarahkan_ke' },
    { sourceValue: 'invest.profit.cepat@outlook.com', targetValue: 'investasi-profit99.site', relationType: 'mengarahkan_ke' },
    { sourceValue: 'hrd.lowongan.bumn@gmail.com', targetValue: 'lowongan-kerja-bumn2026.com', relationType: 'mengarahkan_ke' },
    { sourceValue: 'admin.pinjaman.online88@yahoo.com', targetValue: 'pinjaman-kilat88.com', relationType: 'mengarahkan_ke' },
    // Nomor telepon ↔ nomor telepon (jaringan sindikat)
    { sourceValue: '081234567890', targetValue: '082198765432', relationType: 'satu_jaringan' },
    { sourceValue: '082198765432', targetValue: '085377889900', relationType: 'satu_jaringan' },
    { sourceValue: '085633221100', targetValue: '087744558899', relationType: 'satu_jaringan' },
    { sourceValue: '082177889955', targetValue: '081266778800', relationType: 'satu_jaringan' },
    { sourceValue: '081399887711', targetValue: '089900112233', relationType: 'satu_jaringan' },
    // URL → URL (situs scam saling redirect)
    { sourceValue: 'danakilat.xyz', targetValue: 'pinjaman-kilat88.com', relationType: 'redirect_ke' },
    { sourceValue: 'shopee-hadiah-undian.xyz', targetValue: 'tokopedia-promo-spesial.site', relationType: 'redirect_ke' },
    { sourceValue: 'crypto-profit-guaranteed.id', targetValue: 'investasi-profit99.site', relationType: 'redirect_ke' },
  ];

  let relationsCreated = 0;
  for (const rel of relationPairs) {
    const source = await prisma.entity.findUnique({ where: { value: rel.sourceValue } });
    const target = await prisma.entity.findUnique({ where: { value: rel.targetValue } });
    if (source && target) {
      const existing = await prisma.entityRelation.findFirst({
        where: { sourceId: source.id, targetId: target.id },
      });
      if (!existing) {
        await prisma.entityRelation.create({
          data: {
            sourceId: source.id,
            targetId: target.id,
            relationType: rel.relationType,
          },
        });
        relationsCreated++;
      }
    }
  }
  console.log(`Entity relations seeded: ${relationsCreated} relations`);

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
