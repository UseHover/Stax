package com.hover.stax.transfers

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.schedules.Schedule
import com.hover.stax.utils.Utils

abstract class AbstractFormViewModel(val application: Application, val repo: DatabaseRepo) : ViewModel() {

    val recentContacts = MutableLiveData<List<StaxContact>>()
    val schedule = MutableLiveData<Schedule>()
    val isEditing = MutableLiveData<Boolean>()

    init {
        isEditing.value = true

        if (!repo.allContacts.value.isNullOrEmpty())
            recentContacts.postValue(repo.allContacts.value)
    }

    fun setEditing(editing: Boolean) {
        isEditing.postValue(editing)
    }

    fun saveSchedule(s: Schedule) {
        Utils.logAnalyticsEvent(application.getString(R.string.scheduled_complete, s.type), application.baseContext)
        repo.insert(s)
    }
}