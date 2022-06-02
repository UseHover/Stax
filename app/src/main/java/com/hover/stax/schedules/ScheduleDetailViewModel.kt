package com.hover.stax.schedules

import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.actions.ActionRepo
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.StaxContact
import kotlinx.coroutines.launch

class ScheduleDetailViewModel(val repo: ScheduleRepo, val actionRepo: ActionRepo, val contactRepo: ContactRepo) : ViewModel() {

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