import {
  Controller,
  Post,
  Body,
  UseGuards,
  UseInterceptors,
  UploadedFile,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { CheckService } from './check.service';
import { CheckRequestDto } from './dto/check-request.dto';
import { CheckResultDto } from './dto/check-result.dto';
import { CheckImageResponseDto } from './dto/check-image-response.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiConsumes,
  ApiBody,
} from '@nestjs/swagger';

@ApiTags('Check')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('check')
export class CheckController {
  constructor(private readonly checkService: CheckService) {}

  @Post()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Scan or check entity by manual text input' })
  @ApiResponse({ status: 200, type: CheckResultDto })
  async checkText(@Body() dto: CheckRequestDto): Promise<CheckResultDto> {
    const result = await this.checkService.checkText(dto);
    return {
      message: 'Pemeriksaan selesai',
      ...result,
    } as any;
  }

  @Post('image')
  @HttpCode(HttpStatus.OK)
  @UseInterceptors(FileInterceptor('image'))
  @ApiOperation({ summary: 'Scan screenshot image using AI Vision (OCR)' })
  @ApiConsumes('multipart/form-data')
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        image: {
          type: 'string',
          format: 'binary',
          description: 'Screenshot image file (max 10MB)',
        },
      },
    },
  })
  @ApiResponse({ status: 200, type: CheckImageResponseDto })
  async checkImage(
    @UploadedFile() file: Express.Multer.File,
  ): Promise<CheckImageResponseDto> {
    const result = await this.checkService.checkImage(file);
    return {
      message: 'Pemeriksaan gambar selesai',
      ...result,
    } as any;
  }
}
