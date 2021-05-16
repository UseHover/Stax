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

    protected val recentContacts = MutableLiveData<List<StaxContact>>()
    protected val schedule = MutableLiveData<Schedule>()
    protected val isEditing = MutableLiveData<Boolean>(true)

    init {
        isEditing.postValue(true)
        recentContacts.postValue(repo.allContacts.value)
    }

    fun setEditing(editing: Boolean){
        isEditing.postValue(editing)
    }

    fun saveSchedule(s: Schedule){
        Utils.logAnalyticsEvent(application.getString(R.string.scheduled_complete, s.type), application.baseContext)
        repo.insert(s)
    }

    companion object {
        var type: String = "P2P"
    }
}