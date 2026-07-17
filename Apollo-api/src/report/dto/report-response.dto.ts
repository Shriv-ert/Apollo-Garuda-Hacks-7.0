import { ApiProperty } from '@nestjs/swagger';

export class EntityResponseDto {
  @ApiProperty({ example: 1 })
  id: number;

  @ApiProperty({ example: 'phone' })
  type: string;

  @ApiProperty({ example: '08999888777' })
  value: string;

  @ApiProperty({ example: 'Pinjol Ilegal' })
  category?: string;

  @ApiProperty({ example: 'unknown' })
  status: string;

  @ApiProperty({ example: 45 })
  risk_score: number;

  @ApiProperty({ example: 0 })
  report_count?: number;
}

export class ReportResponseDto {
  @ApiProperty({ example: 1 })
  id: number;

  @ApiProperty({ example: 'pending' })
  status: string;

  @ApiProperty({ example: 'Pinjol Ilegal' })
  category: string;

  @ApiProperty({ example: 'Ditagih dengan ancaman...' })
  description: string;

  @ApiProperty({ example: 'https://example.com/bukti.jpg' })
  proof_image: string;

  @ApiProperty({ example: 'Catatan tim verifikator' })
  review_note?: string;

  @ApiProperty()
  created_at: Date;

  @ApiProperty({ type: [EntityResponseDto] })
  entities: EntityResponseDto[];

  @ApiProperty({ type: EntityResponseDto, required: false })
  entity?: EntityResponseDto;
}
