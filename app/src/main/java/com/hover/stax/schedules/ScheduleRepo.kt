package com.hover.stax.schedules

import androidx.lifecycle.LiveData
import com.hover.stax.database.AppDatabase

class ScheduleRepo(db: AppDatabase) {
    private val scheduleDao: ScheduleDao = db.scheduleDao()

    val futureTransactions: LiveData<List<Schedule>>
        get() = scheduleDao.liveFuture

    fun getFutureTransactions(channelId: Int): LiveData<List<Schedule>> {
        return scheduleDao.getLiveFutureByChannelId(channelId)
    }

    fun getSchedule(id: Int): Schedule? {
        return scheduleDao.get(id)
    }

    fun insert(schedule: Schedule?) {
        AppDatabase.databaseWriteExecutor.execute { schedule?.let { scheduleDao.insert(schedule) } }
    }

    fun update(schedule: Schedule?) {
        AppDatabase.databaseWriteExecutor.execute { scheduleDao.updateSchedule(schedule) }
    }

    fun delete(schedule: Schedule?) {
        AppDatabase.databaseWriteExecutor.execute { scheduleDao.deleteSchedule(schedule) }
    }
}