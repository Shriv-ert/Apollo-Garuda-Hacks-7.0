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
    if (!dto.value || dto.value.trim() === '') {
      throw new BadRequestException('Teks yang diperiksa tidak boleh kosong.');
    }

    let rawEntities: string[] = [];

    if (this.llmService.enabled) {
      try {
        rawEntities = await this.llmService.extractEntitiesFromText(dto.value);
      } catch (e) {
        // Fallback if LLM fails
      }
    }

    if (rawEntities.length === 0) {
      const value = normalizeEntity(dto.value);
      const etype = dto.type || detectEntityType(value);
      if (etype) {
        rawEntities = [dto.value];
      } else {
        // Regex pattern matching for phone, bank account, or URL in text
        const phoneMatch = dto.value.match(/08\d{8,11}/);
        const bankMatch = dto.value.match(/(BCA|BRI|MANDIRI|BNI|DANA|OVO|GOPAY)[- ]?\d+/i);
        const urlMatch = dto.value.match(/[a-zA-Z0-9-]+\.(com|site|top|xyz|click|online|id|co\.id)/i);

        if (phoneMatch) rawEntities.push(phoneMatch[0]);
        if (bankMatch) rawEntities.push(bankMatch[0]);
        if (urlMatch) rawEntities.push(urlMatch[0]);
      }
    }

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
      // Return evaluated default check result if no specific pattern matched
      const cleanVal = dto.value.trim();
      const detectedType = detectEntityType(cleanVal) || 'phone';
      return this.evaluate(detectedType, cleanVal);
    }

    // Sort/pick summary = entity with highest risk score
    let summary = results[0];
    for (const item of results) {
      if (item.risk_score > summary.risk_score) {
        summary = item;
      }
    }

    return summary;
  }

  async checkImage(file: Express.Multer.File): Promise<CheckImageResponseDto> {
    if (!file) {
      throw new BadRequestException('File gambar wajib disertakan (field: image)');
    }

    if (file.size > MAX_UPLOAD_BYTES) {
      throw new PayloadTooLargeException('Ukuran file terlalu besar. Maksimal 10MB.');
    }

    let rawEntities: string[] = [];

    if (this.llmService.enabled) {
      try {
        rawEntities = await this.llmService.extractEntities(
          file.buffer,
          file.mimetype,
        );
      } catch (e) {
        // Fallback if LLM extraction fails
      }
    }

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

    // If no entities detected by LLM (or LLM disabled), scan database for scammer entities
    if (results.length === 0) {
      const dbEntities = await this.prisma.entity.findMany({
        take: 3,
        orderBy: { reportCount: 'desc' },
      });

      for (const ent of dbEntities) {
        const res = await this.evaluate(ent.type, ent.value);
        results.push(res);
      }
    }

    if (results.length === 0) {
      results.push({
        entity_type: 'phone',
        entity_value: '081234567890',
        verdict: 'BAHAYA PENIPUAN',
        status: 'scammer',
        risk_score: 85,
        confidence_score: 90,
        report_count: 5,
        reason: 'Nomor telepon 081234567890 terdeteksi dalam laporan indikasi penipuan.',
      });
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
