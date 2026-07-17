import { ApiProperty } from '@nestjs/swagger';

export class ReportResponseDto {
  @ApiProperty({ example: 'clx12345678' })
  id: string;

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

  @ApiProperty()
  entity: {
    id: string;
    type: string;
    value: string;
    status: string;
    risk_score: number;
  };
}
