import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsString, MinLength } from 'class-validator';

export class ReviewRequestDto {
  @ApiProperty({ example: 'Bukti screenshot terbukti valid dan sesuai dengan laporan penipuan' })
  @IsNotEmpty({ message: 'Catatan peninjauan wajib diisi' })
  @IsString()
  @MinLength(5, { message: 'Catatan peninjauan minimal 5 karakter' })
  review_note: string;
}
