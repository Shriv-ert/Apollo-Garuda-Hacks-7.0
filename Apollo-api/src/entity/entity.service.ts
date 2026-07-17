import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { EntityListQueryDto } from './dto/entity-list-query.dto';

@Injectable()
export class EntityService {
  constructor(private readonly prisma: PrismaService) {}

  async list(query: EntityListQueryDto) {
    const pageNum = Math.max(1, Number(query.page) || 1);
    const limitNum = Math.max(1, Number(query.limit) || 20);
    const skip = (pageNum - 1) * limitNum;

    const where: any = {};

    if (query.type) {
      where.type = query.type;
    }

    if (query.status) {
      where.status = query.status;
    }

    if (query.q && query.q.trim() !== '') {
      where.value = {
        contains: query.q.trim(),
        mode: 'insensitive',
      };
    }

    const [items, total] = await Promise.all([
      this.prisma.entity.findMany({
        where,
        orderBy: [{ riskScore: 'desc' }, { createdAt: 'desc' }],
        skip,
        take: limitNum,
      }),
      this.prisma.entity.count({ where }),
    ]);

    return {
      items: items.map((e) => ({
        id: e.id,
        type: e.type,
        value: e.value,
        category: e.category || undefined,
        status: e.status,
        risk_score: e.riskScore,
        report_count: e.reportCount,
      })),
      pagination: {
        total,
        page: pageNum,
        limit: limitNum,
        total_pages: Math.ceil(total / limitNum),
      },
    };
  }
}
