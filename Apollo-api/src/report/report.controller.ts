import {
  Controller,
  Post,
  Get,
  Body,
  Param,
  Query,
  UseGuards,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ReportService } from './report.service';
import { SubmitReportDto } from './dto/submit-report.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiQuery,
} from '@nestjs/swagger';

@ApiTags('Report')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller()
export class ReportController {
  constructor(private readonly reportService: ReportService) {}

  @Post('report')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Submit a new fraud report' })
  async submit(
    @CurrentUser() user: any,
    @Body() dto: SubmitReportDto,
  ) {
    const data = await this.reportService.submit(user.id, dto);
    return {
      message: 'Laporan berhasil dikirim dan menunggu verifikasi',
      data,
    };
  }

  @Get('reports/history')
  @ApiOperation({ summary: 'View logged-in user report history' })
  @ApiQuery({ name: 'status', required: false, enum: ['pending', 'reviewing', 'verified', 'rejected'] })
  @ApiQuery({ name: 'page', required: false, example: 1 })
  @ApiQuery({ name: 'limit', required: false, example: 10 })
  async history(
    @CurrentUser() user: any,
    @Query('status') status?: string,
    @Query('page') page?: number,
    @Query('limit') limit?: number,
  ) {
    const result = await this.reportService.history(user.id, status, page, limit);
    return {
      message: 'Riwayat laporan berhasil diambil',
      ...result,
    };
  }

  @Get('reports/:id')
  @ApiOperation({ summary: 'View single report detail' })
  async detail(
    @CurrentUser() user: any,
    @Param('id') id: string,
  ) {
    const data = await this.reportService.detail(user.id, id, user.role);
    return {
      message: 'Detail laporan berhasil diambil',
      data,
    };
  }
}
