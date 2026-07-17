import {
  Controller,
  Get,
  Query,
  UseGuards,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { EntityService } from './entity.service';
import { EntityListQueryDto } from './dto/entity-list-query.dto';
import { EntityPaginatedResponseDto } from './dto/entity-response.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
} from '@nestjs/swagger';

@ApiTags('Entities')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('entities')
export class EntityController {
  constructor(private readonly entityService: EntityService) {}

  @Get()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Get list of entities with optional filters' })
  @ApiResponse({ status: 200, type: EntityPaginatedResponseDto })
  async list(@Query() query: EntityListQueryDto) {
    const result = await this.entityService.list(query);
    return {
      message: 'Daftar entitas berhasil diambil',
      ...result,
    };
  }
}
