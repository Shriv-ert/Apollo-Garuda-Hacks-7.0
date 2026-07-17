import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString } from 'class-validator';

export class ReviewRequestDto {
  @ApiPropertyOptional({ example: 'Bukti terverifikasi oleh admin' })
  @IsOptional()
  @IsString()
  review_note?: string;
}
