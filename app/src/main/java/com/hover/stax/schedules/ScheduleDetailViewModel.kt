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
package com.hover.stax.schedules

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.StaxContact
import com.hover.stax.data.local.actions.ActionRepo
import kotlinx.coroutines.launch

class ScheduleDetailViewModel(
    val repo: ScheduleRepo,
    val actionRepo: ActionRepo,
    val contactRepo: ContactRepo
) : ViewModel() {

    val schedule = MutableLiveData<Schedule>()
    var action: LiveData<HoverAction> = MutableLiveData()
    var contacts: LiveData<List<StaxContact>> = MutableLiveData()

    init {
        action = Transformations.switchMap(schedule, this::loadAction)
        contacts = Transformations.switchMap(schedule, this::loadContacts)
    }

    fun setSchedule(id: Int) = viewModelScope.launch { schedule.postValue(repo.getSchedule(id)) }

    private fun loadAction(s: Schedule?): LiveData<HoverAction> = if (s != null) actionRepo.getLiveAction(s.action_id) else MutableLiveData()

    private fun loadContacts(s: Schedule?): LiveData<List<StaxContact>> = if (s != null)
        contactRepo.getLiveContacts(s.recipient_ids.split(",").toTypedArray())
    else
        MutableLiveData()

    fun deleteSchedule() = repo.delete(schedule.value)
}