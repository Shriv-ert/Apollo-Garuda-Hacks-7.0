import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNotEmpty, IsOptional, IsString, IsIn } from 'class-validator';

export class CheckRequestDto {
  @ApiProperty({ example: '08123456789' })
  @IsNotEmpty({ message: 'Nilai entitas wajib diisi' })
  @IsString()
  value: string;

  @ApiPropertyOptional({
    example: 'phone',
    enum: ['phone', 'bank_account', 'url', 'email'],
  })
  @IsOptional()
  @IsIn(['phone', 'bank_account', 'url', 'email'], {
    message: 'Tipe entitas tidak valid',
  })
  type?: 'phone' | 'bank_account' | 'url' | 'email';
}
