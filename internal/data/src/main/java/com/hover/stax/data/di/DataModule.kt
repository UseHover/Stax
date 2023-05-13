/*
 * Copyright 2023 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.data.di

import com.hover.stax.data.accounts.AccountRepository
import com.hover.stax.data.accounts.AccountRepositoryImpl
import com.hover.stax.data.actions.ActionRepo
import com.hover.stax.data.actions.ActionRepository
import com.hover.stax.data.auth.AuthRepository
import com.hover.stax.data.auth.AuthRepositoryImpl
import com.hover.stax.data.bounty.BountyRepository
import com.hover.stax.data.bounty.BountyRepositoryImpl
import com.hover.stax.data.channel.ChannelRepository
import com.hover.stax.data.channel.ChannelRepositoryImpl
import com.hover.stax.data.contact.ContactRepo
import com.hover.stax.data.contact.ContactRepository
import com.hover.stax.data.merchant.MerchantRepo
import com.hover.stax.data.merchant.MerchantRepository
import com.hover.stax.data.parser.ParserRepository
import com.hover.stax.data.paybill.PaybillRepo
import com.hover.stax.data.paybill.PaybillRepository
import com.hover.stax.data.requests.RequestRepo
import com.hover.stax.data.requests.RequestRepository
import com.hover.stax.data.schedule.ScheduleRepo
import com.hover.stax.data.schedule.ScheduleRepository
import com.hover.stax.data.sim.SimInfoRepository
import com.hover.stax.data.sim.SimInfoRepositoryImpl
import com.hover.stax.data.tips.FinancialTipsRepository
import com.hover.stax.data.tips.FinancialTipsRepositoryImpl
import com.hover.stax.data.transactions.TransactionRepo
import com.hover.stax.data.transactions.TransactionRepository
import com.hover.stax.data.user.StaxUserRepository
import com.hover.stax.data.user.StaxUserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindsAccountRepository(accountRepositoryImpl: AccountRepositoryImpl): AccountRepository

    @Binds
    abstract fun bindsActionRepository(actionRepo: ActionRepo): ActionRepository

    @Binds
    abstract fun bindsAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindsBountyRepository(bountyRepositoryImpl: BountyRepositoryImpl): BountyRepository

    @Binds
    abstract fun bindsChannelRepository(channelRepositoryImpl: ChannelRepositoryImpl): ChannelRepository

    @Binds
    abstract fun bindsContactRepository(contactRepo: ContactRepo): ContactRepository

    @Binds
    abstract fun bindsMerchantRepository(merchantRepo: MerchantRepo): MerchantRepository

    @Binds
    abstract fun bindsParserRepository(parserRepository: ParserRepository): ParserRepository

    @Binds
    abstract fun bindsPaybillRepository(paybillRepo: PaybillRepo): PaybillRepository

    @Binds
    abstract fun bindsRequestRepository(requestRepo: RequestRepo): RequestRepository

    @Binds
    abstract fun bindsScheduleRepository(scheduleRepo: ScheduleRepo): ScheduleRepository

    @Binds
    abstract fun bindsSimRepository(simRepositoryImpl: SimInfoRepositoryImpl): SimInfoRepository

    @Binds
    abstract fun bindsFinancialTipsRepository(financialTipsRepositoryImpl: FinancialTipsRepositoryImpl): FinancialTipsRepository

    @Binds
    abstract fun bindsTransactionRepository(transactionRepo: TransactionRepo): TransactionRepository

    @Binds
    abstract fun bindsUserRepository(staxUserRepositoryImpl: StaxUserRepositoryImpl): StaxUserRepository
}