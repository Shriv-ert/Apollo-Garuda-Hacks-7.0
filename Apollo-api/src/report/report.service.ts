import {
  Injectable,
  BadRequestException,
  NotFoundException,
  ForbiddenException,
} from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { normalizeEntity, detectEntityType } from '../common/utils/entity.util';
import { SubmitReportDto } from './dto/submit-report.dto';

@Injectable()
export class ReportService {
  constructor(private readonly prisma: PrismaService) {}

  async submit(userId: any, dto: SubmitReportDto) {
    const uId = Number(userId);
    const normValue = normalizeEntity(dto.entity_value);
    const etype = dto.entity_type || detectEntityType(normValue);

    if (!etype) {
      throw new BadRequestException(
        'Format entitas tidak dikenali. Masukkan nomor telepon, rekening bank, URL, atau email.',
      );
    }

    // Upsert entity into DB (defaults to 'unknown' status if new)
    const entity = await this.prisma.entity.upsert({
      where: { value: normValue },
      update: {},
      create: {
        type: etype,
        value: normValue,
        status: 'unknown',
        riskScore: 45,
        confidenceScore: 40,
        reportCount: 0,
      },
    });

    // Create report record
    const report = await this.prisma.report.create({
      data: {
        userId: uId,
        entityId: entity.id,
        category: dto.category,
        description: dto.description,
        proofImage: dto.proof_image,
        status: 'pending',
      },
      include: {
        entity: true,
      },
    });

    return this.mapReportResponse(report);
  }

  async history(
    userId: any,
    status?: string,
    page: number = 1,
    limit: number = 10,
  ) {
    const uId = Number(userId);
    const pageNum = Math.max(1, Number(page) || 1);
    const limitNum = Math.max(1, Number(limit) || 10);
    const skip = (pageNum - 1) * limitNum;

    const where: any = { userId: uId };
    if (status) {
      where.status = status;
    }

    const [items, total] = await Promise.all([
      this.prisma.report.findMany({
        where,
        orderBy: { createdAt: 'desc' },
        skip,
        take: limitNum,
        include: { entity: true },
      }),
      this.prisma.report.count({ where }),
    ]);

    return {
      items: items.map((item) => this.mapReportResponse(item)),
      pagination: {
        total,
        page: pageNum,
        limit: limitNum,
        total_pages: Math.ceil(total / limitNum),
      },
    };
  }

  async detail(userId: any, reportId: string, userRole?: string) {
    const rId = Number(reportId);
    const uId = Number(userId);

    const report = await this.prisma.report.findUnique({
      where: { id: rId },
      include: { entity: true },
    });

    if (!report) {
      throw new NotFoundException('Laporan tidak ditemukan');
    }

    if (report.userId !== uId && userRole !== 'admin') {
      throw new ForbiddenException('Anda tidak memiliki akses ke laporan ini');
    }

    return this.mapReportResponse(report);
  }

  private mapReportResponse(report: any) {
    return {
      id: report.id,
      status: report.status,
      category: report.category,
      description: report.description,
      proof_image: report.proofImage,
      review_note: report.reviewNote || undefined,
      created_at: report.createdAt,
      entity: {
        id: report.entity.id,
        type: report.entity.type,
        value: report.entity.value,
        status: report.entity.status,
        risk_score: report.entity.riskScore,
      },
    };
  }
}
