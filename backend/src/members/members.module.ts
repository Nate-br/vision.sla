import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { MembersController } from './members.controller';
import { MembersService } from './members.service';
import { Member } from '../auth/entities/member.entity';
import { CustomerProfile } from './entities/customer-profile.entity';
import { KycDocument } from './entities/kyc-document.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Member, CustomerProfile, KycDocument])],
  controllers: [MembersController],
  providers: [MembersService],
  exports: [MembersService],
})
export class MembersModule {}
