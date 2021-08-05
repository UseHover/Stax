package com.hover.stax.futureTransactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.requests.Request
import com.hover.stax.schedules.Schedule

class FutureViewModel(val repo: DatabaseRepo) : ViewModel() {

    var scheduled: LiveData<List<Schedule>> = MutableLiveData()
    var requests: LiveData<List<Request>> = MutableLiveData()

    init {
        scheduled = repo.futureTransactions
        requests = repo.liveRequests
    }

    fun requestsByChannel(channelId: Int): LiveData<List<Request>> = repo.getLiveRequests(channelId)

    fun scheduledByChannel(channelId: Int): LiveData<List<Schedule>> = repo.getFutureTransactions(channelId)
}