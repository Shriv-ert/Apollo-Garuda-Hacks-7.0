import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { GraphResponseDto } from './dto/graph-response.dto';

@Injectable()
export class GraphService {
  constructor(private readonly prisma: PrismaService) {}

  async getGraphData(): Promise<GraphResponseDto> {
    const [entities, relations] = await Promise.all([
      this.prisma.entity.findMany({
        orderBy: { riskScore: 'desc' },
      }),
      this.prisma.entityRelation.findMany(),
    ]);

    const nodes = entities.map((e) => ({
      id: String(e.id),
      type: e.type,
      value: e.value,
      status: e.status,
      risk_score: e.riskScore,
      report_count: e.reportCount,
    }));

    const edges = relations.map((r) => ({
      source_id: String(r.sourceId),
      target_id: String(r.targetId),
      relation_type: r.relationType,
    }));

    return {
      nodes,
      edges,
    };
  }
}
