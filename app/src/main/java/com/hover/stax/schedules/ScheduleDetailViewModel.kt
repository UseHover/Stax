package com.hover.stax.schedules

import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.contacts.StaxContact
import com.hover.stax.database.DatabaseRepo
import kotlinx.coroutines.launch

class ScheduleDetailViewModel(val repo: DatabaseRepo) : ViewModel() {

    val schedule = MutableLiveData<Schedule>()
    var action: LiveData<HoverAction> = MutableLiveData()
    var contacts: LiveData<List<StaxContact>> = MutableLiveData()

    init {
        action = Transformations.switchMap(schedule, this::loadAction)
        contacts = Transformations.switchMap(schedule, this::loadContacts)
    }

    fun setSchedule(id: Int) = viewModelScope.launch { schedule.postValue(repo.getSchedule(id)) }

    private fun loadAction(s: Schedule?): LiveData<HoverAction> = if (s != null) repo.getLiveAction(s.action_id) else MutableLiveData()

    private fun loadContacts(s: Schedule?): LiveData<List<StaxContact>> = if (s != null)
        repo.getLiveContacts(s.recipient_ids.split(",").toTypedArray())
    else
        MutableLiveData()

    fun deleteSchedule() = repo.delete(schedule.value)

}