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
package com.hover.stax.database.di

import com.hover.sdk.sims.SimInfoDao
import com.hover.stax.database.StaxDatabase
import com.hover.stax.database.dao.AccountDao
import com.hover.stax.database.dao.ChannelDao
import com.hover.stax.database.dao.ContactDao
import com.hover.stax.database.dao.MerchantDao
import com.hover.stax.database.dao.PaybillDao
import com.hover.stax.database.dao.RequestDao
import com.hover.stax.database.dao.ScheduleDao
import com.hover.stax.database.dao.TransactionDao
import com.hover.stax.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {

    @Provides
    fun providesAccountDao(
        database: StaxDatabase,
    ): AccountDao = database.accountDao()

    @Provides
    fun providesChannelDao(
        database: StaxDatabase,
    ): ChannelDao = database.channelDao()

    @Provides
    fun providesContactDao(
        database: StaxDatabase,
    ): ContactDao = database.contactDao()

    @Provides
    fun providesMerchantDao(
        database: StaxDatabase,
    ): MerchantDao = database.merchantDao()

    @Provides
    fun providesPaybillDao(
        database: StaxDatabase,
    ): PaybillDao = database.paybillDao()

    @Provides
    fun providesRequestDao(
        database: StaxDatabase,
    ): RequestDao = database.requestDao()

    @Provides
    fun providesScheduleDao(
        database: StaxDatabase,
    ): ScheduleDao = database.scheduleDao()

    @Provides
    fun providesSimDao(
        database: StaxDatabase,
    ): SimInfoDao = database.simInfoDao()

    @Provides
    fun providesTransactionDao(
        database: StaxDatabase,
    ): TransactionDao = database.transactionDao()

    @Provides
    fun providesUserDao(
        database: StaxDatabase,
    ): UserDao = database.userDao()
}