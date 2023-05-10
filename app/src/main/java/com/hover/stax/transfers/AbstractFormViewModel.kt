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
package com.hover.stax.transfers

import android.app.Application
import android.content.Context
import androidx.annotation.CallSuper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hover.stax.R
import com.hover.stax.data.contact.ContactRepo
import com.hover.stax.database.models.StaxContact
import com.hover.stax.database.models.Schedule
import com.hover.stax.data.schedule.ScheduleRepo
import com.hover.stax.core.AnalyticsUtil

abstract class AbstractFormViewModel(
    application: Application,
    val contactRepo: ContactRepo,
    private val scheduleRepo: ScheduleRepo
) : AndroidViewModel(application) {

    var recentContacts: LiveData<List<StaxContact>> = MutableLiveData()
    val schedule = MutableLiveData<Schedule>()
    val isEditing = MutableLiveData(true)

    init {
        isEditing.value = true
        recentContacts = contactRepo.allContacts
    }

    fun setEditing(editing: Boolean) {
        isEditing.postValue(editing)
    }

    fun load(s: Schedule) {
        schedule.postValue(s)
    }

    fun saveSchedule(s: Schedule) {
        com.hover.stax.core.AnalyticsUtil.logAnalyticsEvent((getApplication() as Context).getString(R.string.scheduled_complete, s.type), getApplication())
        scheduleRepo.insert(s)
    }

    fun getString(stringId: Int): String {
        return (getApplication() as Context).getString(stringId)
    }

    @CallSuper
    open fun reset() { isEditing.postValue(true) }
}