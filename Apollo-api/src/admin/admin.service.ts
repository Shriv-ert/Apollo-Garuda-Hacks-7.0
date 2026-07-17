import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { ReviewRequestDto } from './dto/review-request.dto';

@Injectable()
export class AdminService {
  constructor(private readonly prisma: PrismaService) {}

  async list(status?: string, page: number = 1, limit: number = 10) {
    const pageNum = Math.max(1, Number(page) || 1);
    const limitNum = Math.max(1, Number(limit) || 10);
    const skip = (pageNum - 1) * limitNum;

    const where: any = {};
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
          user: {
            select: {
              id: true,
              email: true,
              fullName: true,
            },
          },
          entity: true,
        },
      }),
      this.prisma.report.count({ where }),
    ]);

    return {
      items: items.map((item) => this.mapAdminReportResponse(item)),
      pagination: {
        total,
        page: pageNum,
        limit: limitNum,
        total_pages: Math.ceil(total / limitNum),
      },
    };
  }

  async detail(reportId: string) {
    const rId = Number(reportId);
    const report = await this.prisma.report.findUnique({
      where: { id: rId },
      include: {
        user: {
          select: {
            id: true,
            email: true,
            fullName: true,
          },
        },
        entity: true,
      },
    });

    if (!report) {
      throw new NotFoundException('Laporan tidak ditemukan');
    }

    return this.mapAdminReportResponse(report);
  }

  async markReviewing(adminId: any, reportId: string) {
    const rId = Number(reportId);
    const aId = Number(adminId);

    const report = await this.prisma.report.findUnique({
      where: { id: rId },
    });

    if (!report) {
      throw new NotFoundException('Laporan tidak ditemukan');
    }

    const updated = await this.prisma.report.update({
      where: { id: rId },
      data: {
        status: 'reviewing',
        reviewedById: aId,
      },
      include: {
        user: { select: { id: true, email: true, fullName: true } },
        entity: true,
      },
    });

    return this.mapAdminReportResponse(updated);
  }

  async verify(adminId: any, reportId: string, dto: ReviewRequestDto) {
    const rId = Number(reportId);
    const aId = Number(adminId);

    const report = await this.prisma.report.findUnique({
      where: { id: rId },
    });

    if (!report) {
      throw new NotFoundException('Laporan tidak ditemukan');
    }

    const result = await this.prisma.$transaction(async (tx) => {
      // 1. Update report status to verified
      const updatedReport = await tx.report.update({
        where: { id: rId },
        data: {
          status: 'verified',
          reviewedById: aId,
          reviewNote: dto.review_note,
          reviewedAt: new Date(),
        },
        include: {
          user: { select: { id: true, email: true, fullName: true } },
          entity: true,
        },
      });

      // 2. Recompute entity scores & status
      await this.recomputeEntity(tx, updatedReport.entityId);

      // 3. Link graph relations for verified entities by same user
      await this.linkRelations(tx, updatedReport.userId, updatedReport.entityId);

      // Fetch fresh updated report with updated entity
      return tx.report.findUnique({
        where: { id: rId },
        include: {
          user: { select: { id: true, email: true, fullName: true } },
          entity: true,
        },
      });
    });

    return this.mapAdminReportResponse(result);
  }

  async reject(adminId: any, reportId: string, dto: ReviewRequestDto) {
    const rId = Number(reportId);
    const aId = Number(adminId);

    const report = await this.prisma.report.findUnique({
      where: { id: rId },
    });

    if (!report) {
      throw new NotFoundException('Laporan tidak ditemukan');
    }

    const updated = await this.prisma.report.update({
      where: { id: rId },
      data: {
        status: 'rejected',
        reviewedById: aId,
        reviewNote: dto.review_note,
        reviewedAt: new Date(),
      },
      include: {
        user: { select: { id: true, email: true, fullName: true } },
        entity: true,
      },
    });

    return this.mapAdminReportResponse(updated);
  }

  async dashboard() {
    const [pending, reviewing, verified, rejected, totalEntities, totalUsers, reportsToday] =
      await Promise.all([
        this.prisma.report.count({ where: { status: 'pending' } }),
        this.prisma.report.count({ where: { status: 'reviewing' } }),
        this.prisma.report.count({ where: { status: 'verified' } }),
        this.prisma.report.count({ where: { status: 'rejected' } }),
        this.prisma.entity.count(),
        this.prisma.user.count(),
        this.prisma.report.count({
          where: {
            createdAt: {
              gte: new Date(new Date().setHours(0, 0, 0, 0)),
            },
          },
        }),
      ]);

    const totalReports = pending + reviewing + verified + rejected;

    return {
      total_reports: totalReports,
      by_status: {
        pending,
        reviewing,
        verified,
        rejected,
      },
      total_entities: totalEntities,
      total_users: totalUsers,
      reports_today: reportsToday,
    };
  }

  private async recomputeEntity(tx: any, entityId: number) {
    const verifiedCount = await tx.report.count({
      where: {
        entityId,
        status: 'verified',
      },
    });

    const entity = await tx.entity.findUnique({
      where: { id: entityId },
    });

    if (!entity) return;

    const riskScore = Math.min(100, verifiedCount * 15);
    const confidenceScore = Math.min(100, 40 + verifiedCount * 10);

    let newStatus = entity.status;
    if (entity.status !== 'ojk_verified' && entity.status !== 'ojk_illegal') {
      newStatus = 'scammer';
    }

    await tx.entity.update({
      where: { id: entityId },
      data: {
        reportCount: verifiedCount,
        riskScore,
        confidenceScore,
        status: newStatus,
      },
    });
  }

  private async linkRelations(tx: any, userId: number, targetEntityId: number) {
    const otherVerifiedReports = await tx.report.findMany({
      where: {
        userId,
        status: 'verified',
        entityId: { not: targetEntityId },
      },
      select: { entityId: true },
    });

    for (const other of otherVerifiedReports) {
      const existing = await tx.entityRelation.findFirst({
        where: {
          OR: [
            { sourceId: targetEntityId, targetId: other.entityId },
            { sourceId: other.entityId, targetId: targetEntityId },
          ],
        },
      });

      if (!existing) {
        await tx.entityRelation.create({
          data: {
            sourceId: targetEntityId,
            targetId: other.entityId,
            relationType: 'terkait_laporan_sama',
          },
        });
      }
    }
  }

  private mapAdminReportResponse(report: any) {
    return {
      id: report.id,
      status: report.status,
      category: report.category,
      description: report.description,
      proof_image: report.proofImage,
      review_note: report.reviewNote || undefined,
      created_at: report.createdAt,
      reviewed_at: report.reviewedAt || undefined,
      user: report.user
        ? {
            id: report.user.id,
            email: report.user.email,
            full_name: report.user.fullName,
          }
        : undefined,
      entity: {
        id: report.entity.id,
        type: report.entity.type,
        value: report.entity.value,
        status: report.entity.status,
        risk_score: report.entity.riskScore,
        confidence_score: report.entity.confidenceScore,
        report_count: report.entity.reportCount,
      },
    };
  }
}
