package com.hover.stax.channels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.hover.sdk.actions.HoverAction
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.transactions.StaxTransaction
import java.util.*


class ChannelDetailViewModel(val repo: DatabaseRepo) : ViewModel() {

    private val id = MutableLiveData<Int>()
    var channel: LiveData<Channel> = MutableLiveData()
    var transactions: LiveData<List<StaxTransaction>> = MutableLiveData()
    var actions: LiveData<List<HoverAction>> = MutableLiveData()
    var spentThisMonth: LiveData<Double> = MutableLiveData()
    var feesThisYear: LiveData<Double> = MutableLiveData()

    private val calendar = Calendar.getInstance()

    init {
        channel = Transformations.switchMap(id, repo::getLiveChannel)
        transactions = Transformations.switchMap(id, repo::getChannelTransactions)
        actions = Transformations.switchMap(id, repo::getChannelActions)
        spentThisMonth = Transformations.switchMap(id, this::loadSpentThisMonth)
        feesThisYear = Transformations.switchMap(id, this::loadFeesThisYear)
    }

    fun setChannel(channelId: Int) = id.postValue(channelId)

    private fun loadSpentThisMonth(id: Int): LiveData<Double>? = repo.getSpentAmount(id, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))

    private fun loadFeesThisYear(id: Int): LiveData<Double>? = repo.getFees(id, calendar.get(Calendar.YEAR))
}