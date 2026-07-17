import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { PrismaModule } from './prisma/prisma.module';
import { AuthModule } from './auth/auth.module';
import { CheckModule } from './check/check.module';
import { ReportModule } from './report/report.module';
import { AdminModule } from './admin/admin.module';
import { GraphModule } from './graph/graph.module';
import { EntityModule } from './entity/entity.module';
import { ConfigModule } from '@nestjs/config';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
    }),
    PrismaModule,
    AuthModule,
    CheckModule,
    ReportModule,
    AdminModule,
    GraphModule,
    EntityModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
