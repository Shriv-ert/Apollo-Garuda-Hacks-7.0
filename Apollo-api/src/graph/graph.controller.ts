import { Controller, Get, UseGuards } from '@nestjs/common';
import { GraphService } from './graph.service';
import { GraphResponseDto } from './dto/graph-response.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
} from '@nestjs/swagger';

@ApiTags('Graph')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('graph')
export class GraphController {
  constructor(private readonly graphService: GraphService) {}

  @Get()
  @ApiOperation({ summary: 'Get fraud network graph (nodes & edges)' })
  @ApiResponse({ status: 200, type: GraphResponseDto })
  async getGraph() {
    const data = await this.graphService.getGraphData();
    return {
      message: 'Data graf jaringan penipuan berhasil diambil',
      data,
    };
  }
}
