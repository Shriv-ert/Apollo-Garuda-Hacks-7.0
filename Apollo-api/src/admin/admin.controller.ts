import {
  Controller,
  Get,
  Put,
  Body,
  Param,
  Query,
  UseGuards,
} from '@nestjs/common';
import { AdminService } from './admin.service';
import { ReviewRequestDto } from './dto/review-request.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiQuery,
} from '@nestjs/swagger';

@ApiTags('Admin')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles('admin')
@Controller('admin')
export class AdminController {
  constructor(private readonly adminService: AdminService) {}

  @Get('dashboard')
  @ApiOperation({ summary: 'Get admin dashboard statistics' })
  async dashboard() {
    const data = await this.adminService.dashboard();
    return {
      message: 'Statistik dashboard berhasil diambil',
      data,
    };
  }

  @Get('reports')
  @ApiOperation({ summary: 'List all user reports for admin review' })
  @ApiQuery({ name: 'status', required: false, enum: ['pending', 'reviewing', 'verified', 'rejected'] })
  @ApiQuery({ name: 'page', required: false, example: 1 })
  @ApiQuery({ name: 'limit', required: false, example: 10 })
  async list(
    @Query('status') status?: string,
    @Query('page') page?: number,
    @Query('limit') limit?: number,
  ) {
    const result = await this.adminService.list(status, page, limit);
    return {
      message: 'Daftar laporan berhasil diambil',
      ...result,
    };
  }

  @Get('reports/:id')
  @ApiOperation({ summary: 'View report detail for admin' })
  async detail(@Param('id') id: string) {
    const data = await this.adminService.detail(id);
    return {
      message: 'Detail laporan berhasil diambil',
      data,
    };
  }

  @Put('reports/:id/reviewing')
  @ApiOperation({ summary: 'Mark report status as reviewing' })
  async markReviewing(
    @CurrentUser() user: any,
    @Param('id') id: string,
  ) {
    const data = await this.adminService.markReviewing(user.id, id);
    return {
      message: 'Status laporan diubah menjadi sedang ditinjau',
      data,
    };
  }

  @Put('reports/:id/verify')
  @ApiOperation({ summary: 'Verify report and update entity score & graph edges' })
  async verify(
    @CurrentUser() user: any,
    @Param('id') id: string,
    @Body() dto: ReviewRequestDto,
  ) {
    const data = await this.adminService.verify(user.id, id, dto);
    return {
      message: 'Laporan berhasil diverifikasi',
      data,
    };
  }

  @Put('reports/:id/reject')
  @ApiOperation({ summary: 'Reject report with review note' })
  async reject(
    @CurrentUser() user: any,
    @Param('id') id: string,
    @Body() dto: ReviewRequestDto,
  ) {
    const data = await this.adminService.reject(user.id, id, dto);
    return {
      message: 'Laporan berhasil ditolak',
      data,
    };
  }
}
