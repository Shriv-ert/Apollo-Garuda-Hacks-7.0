import { ApiProperty } from '@nestjs/swagger';

export class EntityItemDto {
  @ApiProperty({ example: 1 })
  id: number;

  @ApiProperty({ example: 'phone' })
  type: string;

  @ApiProperty({ example: '08123456789' })
  value: string;

  @ApiProperty({ example: 'Pinjol Ilegal', required: false })
  category?: string;

  @ApiProperty({ example: 'scammer' })
  status: string;

  @ApiProperty({ example: 75 })
  risk_score: number;

  @ApiProperty({ example: 5 })
  report_count: number;
}

export class EntityPaginatedResponseDto {
  @ApiProperty({ type: [EntityItemDto] })
  items: EntityItemDto[];

  @ApiProperty({
    example: {
      total: 10,
      page: 1,
      limit: 20,
      total_pages: 1,
    },
  })
  pagination: {
    total: number;
    page: number;
    limit: number;
    total_pages: number;
  };
}
