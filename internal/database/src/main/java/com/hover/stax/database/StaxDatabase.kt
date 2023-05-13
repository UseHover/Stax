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
package com.hover.stax.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hover.sdk.sims.SimInfoDao
import com.hover.stax.database.converters.Converters
import com.hover.stax.database.dao.AccountDao
import com.hover.stax.database.dao.ChannelDao
import com.hover.stax.database.dao.ContactDao
import com.hover.stax.database.dao.MerchantDao
import com.hover.stax.database.dao.PaybillDao
import com.hover.stax.database.dao.RequestDao
import com.hover.stax.database.dao.ScheduleDao
import com.hover.stax.database.dao.TransactionDao
import com.hover.stax.database.dao.UserDao
import com.hover.stax.database.models.Account
import com.hover.stax.database.models.Merchant
import com.hover.stax.database.models.Paybill
import com.hover.stax.database.models.Request
import com.hover.stax.database.models.Schedule
import com.hover.stax.database.models.StaxContact
import com.hover.stax.database.models.StaxTransaction
import com.hover.stax.database.models.StaxUser
import kotlinx.coroutines.channels.Channel

@Database(
    entities = [
        Channel::class, StaxTransaction::class, StaxContact::class, Request::class, Schedule::class, Account::class, Paybill::class, Merchant::class, StaxUser::class
    ],
    version = 1, // TODO - match previous database here
    autoMigrations = [],
    exportSchema = true,
)
@TypeConverters(
    Converters::class
)
abstract class StaxDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao

    abstract fun channelDao(): ChannelDao

    abstract fun contactDao(): ContactDao

    abstract fun merchantDao(): MerchantDao

    abstract fun paybillDao(): PaybillDao

    abstract fun requestDao(): RequestDao

    abstract fun scheduleDao(): ScheduleDao

    abstract fun simInfoDao(): SimInfoDao

    abstract fun transactionDao(): TransactionDao

    abstract fun userDao(): UserDao
}