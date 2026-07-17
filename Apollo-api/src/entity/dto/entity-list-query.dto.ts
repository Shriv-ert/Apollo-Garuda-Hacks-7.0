import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString, IsIn, IsInt, Min } from 'class-validator';
import { Type } from 'class-transformer';

export class EntityListQueryDto {
  @ApiPropertyOptional({
    example: 'phone',
    enum: ['phone', 'bank_account', 'url', 'email'],
  })
  @IsOptional()
  @IsIn(['phone', 'bank_account', 'url', 'email'], {
    message: 'Tipe entitas tidak valid',
  })
  type?: 'phone' | 'bank_account' | 'url' | 'email';

  @ApiPropertyOptional({
    example: 'scammer',
    enum: ['ojk_verified', 'ojk_illegal', 'scammer', 'unknown'],
  })
  @IsOptional()
  @IsIn(['ojk_verified', 'ojk_illegal', 'scammer', 'unknown'], {
    message: 'Status entitas tidak valid',
  })
  status?: string;

  @ApiPropertyOptional({ example: 'danakilat' })
  @IsOptional()
  @IsString()
  q?: string;

  @ApiPropertyOptional({ example: 1, default: 1 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  page?: number = 1;

  @ApiPropertyOptional({ example: 20, default: 20 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  limit?: number = 20;
}
