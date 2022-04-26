package com.hover.stax.transfers

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hover.stax.R
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.StaxContact
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.schedules.Schedule
import com.hover.stax.utils.AnalyticsUtil

abstract class AbstractFormViewModel(application: Application, val contactRepo: ContactRepo, val scheduleRepo: ScheduleRepo) : AndroidViewModel(application) {

    var recentContacts: LiveData<List<StaxContact>> = MutableLiveData()
    val schedule = MutableLiveData<Schedule>()
    val isEditing = MutableLiveData(false)

    init {
        isEditing.value = true
        recentContacts = contactRepo.allContacts
    }

    fun setEditing(editing: Boolean) {
        isEditing.postValue(editing)
    }

    fun saveSchedule(s: Schedule) {
        AnalyticsUtil.logAnalyticsEvent((getApplication() as Context).getString(R.string.scheduled_complete, s.type), getApplication())
        scheduleRepo.insert(s)
    }

    fun getString(stringId: Int): String {
        return (getApplication() as Context).getString(stringId)
    }
}