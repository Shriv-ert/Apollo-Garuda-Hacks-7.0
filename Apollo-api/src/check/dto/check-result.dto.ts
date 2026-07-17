import { ApiProperty } from '@nestjs/swagger';

export class CheckResultDto {
  @ApiProperty({ example: 'phone' })
  entity_type: string;

  @ApiProperty({ example: '08123456789' })
  entity_value: string;

  @ApiProperty({ example: 'BAHAYA PENIPUAN' })
  verdict: string;

  @ApiProperty({ example: 'scammer' })
  status: string;

  @ApiProperty({ example: 75 })
  risk_score: number;

  @ApiProperty({ example: 90 })
  confidence_score: number;

  @ApiProperty({ example: 5 })
  report_count: number;

  @ApiProperty({ example: '08123456789 ditemukan dalam 5 laporan terverifikasi.' })
  reason: string;
}
