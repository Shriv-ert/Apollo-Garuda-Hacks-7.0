import { ApiProperty } from '@nestjs/swagger';
import { CheckResultDto } from './check-result.dto';

export class CheckImageResponseDto {
  @ApiProperty({ type: CheckResultDto })
  summary: CheckResultDto;

  @ApiProperty({ type: [CheckResultDto] })
  entities: CheckResultDto[];
}
