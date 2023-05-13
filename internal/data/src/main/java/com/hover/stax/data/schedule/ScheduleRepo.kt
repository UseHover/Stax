/*
 * Copyright 2022 Stax
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
package com.hover.stax.data.schedule

import androidx.lifecycle.LiveData
import com.hover.stax.database.dao.ScheduleDao
import com.hover.stax.database.models.Schedule
import javax.inject.Inject

interface ScheduleRepository {

    val futureTransactions: LiveData<List<Schedule>>

    fun getFutureTransactions(channelId: Int): LiveData<List<Schedule>>

    fun getSchedule(id: Int): Schedule?

    fun insert(schedule: Schedule?)

    fun update(schedule: Schedule?)

    fun delete(schedule: Schedule?)
}

class ScheduleRepo @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override val futureTransactions: LiveData<List<Schedule>>
        get() = scheduleDao.liveFuture

    override fun getFutureTransactions(channelId: Int): LiveData<List<Schedule>> {
        return scheduleDao.getLiveFutureByChannelId(channelId)
    }

    override fun getSchedule(id: Int): Schedule? {
        return scheduleDao.get(id)
    }

    override fun insert(schedule: Schedule?) {
        schedule?.let { scheduleDao.insert(schedule) }
    }

    override fun update(schedule: Schedule?) {
        scheduleDao.updateSchedule(schedule)
    }

    override fun delete(schedule: Schedule?) {
        scheduleDao.deleteSchedule(schedule)
    }
}