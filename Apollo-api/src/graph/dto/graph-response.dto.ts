import { ApiProperty } from '@nestjs/swagger';

export class GraphNodeDto {
  @ApiProperty({ example: 'clx123' })
  id: string;

  @ApiProperty({ example: 'phone' })
  type: string;

  @ApiProperty({ example: '08123456789' })
  value: string;

  @ApiProperty({ example: 'scammer' })
  status: string;

  @ApiProperty({ example: 75 })
  risk_score: number;

  @ApiProperty({ example: 5 })
  report_count: number;
}

export class GraphEdgeDto {
  @ApiProperty({ example: 'clx123' })
  source_id: string;

  @ApiProperty({ example: 'clx456' })
  target_id: string;

  @ApiProperty({ example: 'terkait_laporan_sama' })
  relation_type: string;
}

export class GraphResponseDto {
  @ApiProperty({ type: [GraphNodeDto] })
  nodes: GraphNodeDto[];

  @ApiProperty({ type: [GraphEdgeDto] })
  edges: GraphEdgeDto[];
}
