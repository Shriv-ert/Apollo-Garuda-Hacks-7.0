import {
  Injectable,
  BadRequestException,
  ServiceUnavailableException,
  PayloadTooLargeException,
  UnprocessableEntityException,
} from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { LlmService } from '../llm/llm.service';
import { normalizeEntity, detectEntityType } from '../common/utils/entity.util';
import { CheckRequestDto } from './dto/check-request.dto';
import { CheckResultDto } from './dto/check-result.dto';
import { CheckImageResponseDto } from './dto/check-image-response.dto';

const MAX_UPLOAD_BYTES = 10 * 1024 * 1024; // 10MB

@Injectable()
export class CheckService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly llmService: LlmService,
  ) {}

  async checkText(dto: CheckRequestDto): Promise<CheckResultDto> {
    const value = normalizeEntity(dto.value);
    const etype = dto.type || detectEntityType(value);

    if (!etype) {
      throw new BadRequestException(
        'Format entitas tidak dikenali. Masukkan nomor telepon, rekening bank, URL, atau email.',
      );
    }

    return this.evaluate(etype, value);
  }

  async checkImage(file: Express.Multer.File): Promise<CheckImageResponseDto> {
    if (!this.llmService.enabled) {
      throw new ServiceUnavailableException('Fitur pindai gambar belum tersedia.');
    }

    if (!file) {
      throw new BadRequestException('File gambar wajib disertakan (field: image)');
    }

    if (file.size > MAX_UPLOAD_BYTES) {
      throw new PayloadTooLargeException('Ukuran file terlalu besar. Maksimal 10MB.');
    }

    const rawEntities = await this.llmService.extractEntities(
      file.buffer,
      file.mimetype,
    );

    const seen = new Set<string>();
    const results: CheckResultDto[] = [];

    for (const raw of rawEntities) {
      const value = normalizeEntity(raw);
      const etype = detectEntityType(value);

      if (!etype || seen.has(value)) {
        continue;
      }

      seen.add(value);
      const res = await this.evaluate(etype, value);
      results.push(res);
    }

    if (results.length === 0) {
      throw new UnprocessableEntityException(
        'Tidak dapat mendeteksi entitas dari gambar. Coba input manual.',
      );
    }

    // Sort/pick summary = entity with highest risk score
    let summary = results[0];
    for (const item of results) {
      if (item.risk_score > summary.risk_score) {
        summary = item;
      }
    }

    return {
      summary,
      entities: results,
    };
  }

  async evaluate(etype: string, value: string): Promise<CheckResultDto> {
    const entity = await this.prisma.entity.findUnique({
      where: { value },
    });

    if (!entity) {
      return {
        entity_type: etype,
        entity_value: value,
        verdict: 'WASPADA',
        status: 'unknown',
        risk_score: 45,
        confidence_score: 40,
        report_count: 0,
        reason: `Entitas ${value} tidak terdaftar di OJK dan belum pernah dilaporkan oleh pengguna lain. Berhati-hatilah.`,
      };
    }

    let verdict = 'WASPADA';
    let riskScore = entity.riskScore;
    let confidenceScore = entity.confidenceScore;
    let reason = '';

    switch (entity.status) {
      case 'ojk_verified':
        verdict = 'AMAN';
        riskScore = 0;
        confidenceScore = 95;
        reason = `${entity.value} terdaftar dan diawasi oleh OJK. Status: resmi dan terpercaya.`;
        break;

      case 'ojk_illegal':
        verdict = 'BAHAYA PENIPUAN';
        riskScore = 95;
        confidenceScore = 95;
        reason = `${entity.value} tercatat dalam daftar entitas ilegal resmi OJK.`;
        break;

      case 'scammer':
        verdict = 'BAHAYA PENIPUAN';
        reason = `${entity.value} tidak terdaftar di OJK dan ditemukan dalam ${entity.reportCount} laporan terverifikasi dari pengguna.`;
        break;

      default:
        verdict = 'WASPADA';
        riskScore = 45;
        confidenceScore = 40;
        reason = `${entity.value} belum terverifikasi. Belum cukup data untuk memberikan keputusan final.`;
        break;
    }

    return {
      entity_type: entity.type || etype,
      entity_value: entity.value,
      verdict,
      status: entity.status,
      risk_score: riskScore,
      confidence_score: confidenceScore,
      report_count: entity.reportCount,
      reason,
    };
  }
}
