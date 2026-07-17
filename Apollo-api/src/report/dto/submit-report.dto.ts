import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsNotEmpty,
  IsOptional,
  IsString,
  MinLength,
  IsIn,
  IsArray,
  ValidateNested,
} from 'class-validator';
import { Type } from 'class-transformer';

export class EntityInputDto {
  @ApiProperty({ example: '08999888777' })
  @IsNotEmpty({ message: 'Nilai entitas wajib diisi' })
  @IsString()
  entity_value: string;

  @ApiPropertyOptional({
    example: 'phone',
    enum: ['phone', 'bank_account', 'url', 'email'],
  })
  @IsOptional()
  @IsIn(['phone', 'bank_account', 'url', 'email'], {
    message: 'Tipe entitas tidak valid',
  })
  entity_type?: 'phone' | 'bank_account' | 'url' | 'email';
}

export class SubmitReportDto {
  @ApiPropertyOptional({
    type: [EntityInputDto],
    example: [
      { entity_value: '08999888777', entity_type: 'phone' },
      { entity_value: 'BCA 12345678', entity_type: 'bank_account' },
    ],
  })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => EntityInputDto)
  entities?: EntityInputDto[];

  @ApiPropertyOptional({ example: '08999888777' })
  @IsOptional()
  @IsString()
  entity_value?: string;

  @ApiPropertyOptional({
    example: 'phone',
    enum: ['phone', 'bank_account', 'url', 'email'],
  })
  @IsOptional()
  @IsIn(['phone', 'bank_account', 'url', 'email'], {
    message: 'Tipe entitas tidak valid',
  })
  entity_type?: 'phone' | 'bank_account' | 'url' | 'email';

  @ApiProperty({ example: 'Pinjol Ilegal' })
  @IsNotEmpty({ message: 'Kategori laporan wajib dipilih' })
  @IsString()
  category: string;

  @ApiProperty({ example: 'Ditagih dengan ancaman padahal tidak pernah pinjam' })
  @IsNotEmpty({ message: 'Kronologi kejadian wajib diisi' })
  @MinLength(10, { message: 'Kronologi kejadian minimal 10 karakter' })
  description: string;

  @ApiProperty({ example: 'https://example.com/bukti.jpg' })
  @IsNotEmpty({ message: 'Bukti tangkapan layar wajib disertakan' })
  @IsString()
  proof_image: string;
}
