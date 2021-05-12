package com.hover.stax.transfers

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.schedules.Schedule

abstract class AbstractFormViewModel(val repo: DatabaseRepo) : ViewModel() {

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
        repo.insert(s)
    }

    companion object {
        const val type: String = "P2P"
    }
}