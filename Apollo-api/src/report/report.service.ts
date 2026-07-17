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

    // Collect input entities (either from dto.entities array or legacy dto.entity_value)
    const rawEntities: Array<{ entity_value: string; entity_type?: string }> = [];
    if (dto.entities && dto.entities.length > 0) {
      rawEntities.push(...dto.entities);
    } else if (dto.entity_value) {
      rawEntities.push({
        entity_value: dto.entity_value,
        entity_type: dto.entity_type,
      });
    }

    if (rawEntities.length === 0) {
      throw new BadRequestException('Entitas yang dilaporkan wajib diisi');
    }

    // Validate & normalize all entities
    const processedEntities: Array<{ normValue: string; etype: string }> = [];
    for (const item of rawEntities) {
      const normValue = normalizeEntity(item.entity_value);
      const etype = item.entity_type || detectEntityType(normValue);
      if (!etype) {
        throw new BadRequestException(
          'Format entitas tidak dikenali. Masukkan nomor telepon, rekening bank, URL, atau email.',
        );
      }
      processedEntities.push({ normValue, etype });
    }

    // Perform database operations in a transaction
    const report = await this.prisma.$transaction(async (tx) => {
      // 1. Create Report row
      const newReport = await tx.report.create({
        data: {
          userId: uId,
          category: dto.category,
          description: dto.description,
          proofImage: dto.proof_image,
          status: 'pending',
        },
      });

      // 2. Upsert each entity & create join row
      for (const { normValue, etype } of processedEntities) {
        const entity = await tx.entity.upsert({
          where: { value: normValue },
          update: {
            category: dto.category,
          },
          create: {
            type: etype,
            value: normValue,
            category: dto.category,
            status: 'unknown',
            riskScore: 45,
            confidenceScore: 40,
            reportCount: 0,
          },
        });

        await tx.reportEntity.upsert({
          where: {
            reportId_entityId: {
              reportId: newReport.id,
              entityId: entity.id,
            },
          },
          update: {},
          create: {
            reportId: newReport.id,
            entityId: entity.id,
          },
        });
      }

      return tx.report.findUnique({
        where: { id: newReport.id },
        include: {
          reportEntities: {
            include: { entity: true },
          },
        },
      });
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
        include: {
          reportEntities: {
            include: { entity: true },
          },
        },
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
      include: {
        reportEntities: {
          include: { entity: true },
        },
      },
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
    const entitiesList = (report.reportEntities || []).map((re: any) => ({
      id: re.entity.id,
      type: re.entity.type,
      value: re.entity.value,
      category: re.entity.category || report.category,
      status: re.entity.status,
      risk_score: re.entity.riskScore,
    }));

    return {
      id: report.id,
      status: report.status,
      category: report.category,
      description: report.description,
      proof_image: report.proofImage,
      review_note: report.reviewNote || undefined,
      created_at: report.createdAt,
      entities: entitiesList,
      entity: entitiesList.length > 0 ? entitiesList[0] : undefined,
    };
  }
}
