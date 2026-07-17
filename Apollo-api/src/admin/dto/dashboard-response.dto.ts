import { ApiProperty } from '@nestjs/swagger';

export class DashboardResponseDto {
  @ApiProperty({ example: 42 })
  total_reports: number;

  @ApiProperty({
    example: { pending: 10, reviewing: 5, verified: 22, rejected: 5 },
  })
  by_status: {
    pending: number;
    reviewing: number;
    verified: number;
    rejected: number;
  };

  @ApiProperty({ example: 15 })
  total_entities: number;

  @ApiProperty({ example: 100 })
  total_users: number;

  @ApiProperty({ example: 3 })
  reports_today: number;
}
