import { ApiProperty } from '@nestjs/swagger';
import { IsEmail, IsNotEmpty, MinLength, MaxLength } from 'class-validator';

export class RegisterDto {
  @ApiProperty({ example: 'user@awam.id' })
  @IsEmail({}, { message: 'Format email tidak valid' })
  @IsNotEmpty({ message: 'Email tidak boleh kosong' })
  email: string;

  @ApiProperty({ example: 'Nama Lengkap' })
  @IsNotEmpty({ message: 'Nama lengkap wajib diisi' })
  @MinLength(2, { message: 'Nama lengkap minimal 2 karakter' })
  @MaxLength(100, { message: 'Nama lengkap maksimal 100 karakter' })
  full_name: string;

  @ApiProperty({ example: 'password123' })
  @IsNotEmpty({ message: 'Password tidak boleh kosong' })
  @MinLength(8, { message: 'Password minimal 8 karakter' })
  password: string;
}
