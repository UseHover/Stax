package com.hover.stax.transfers

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.schedules.Schedule
import com.hover.stax.utils.Utils

abstract class AbstractFormViewModel(val application: Application, val repo: DatabaseRepo) : ViewModel() {

    var recentContacts: LiveData<List<StaxContact>> = MutableLiveData()
    val schedule = MutableLiveData<Schedule>()
    val isEditing = MutableLiveData(false)

    init {
        isEditing.value = true
        recentContacts = repo.allContacts
    }

    fun setEditing(editing: Boolean) {
        isEditing.postValue(editing)
    }

    fun saveSchedule(s: Schedule) {
        Utils.logAnalyticsEvent(application.getString(R.string.scheduled_complete, s.type), application.baseContext)
        repo.insert(s)
    }
}