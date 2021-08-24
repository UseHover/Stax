package com.hover.stax.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.channels.Channel
import com.hover.stax.database.DatabaseRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PinsViewModel(val repo: DatabaseRepo) : ViewModel() {

    private var channels: LiveData<List<Channel>> = MutableLiveData()
    var channel: LiveData<Channel> = MutableLiveData()

    init {
        loadSelectedChannels()
    }

    fun getSelectedChannels(): LiveData<List<Channel>> = channels

    private fun loadSelectedChannels() {
        channels = repo.selected
    }

    fun loadChannel(id: Int) {
        channel = repo.getLiveChannel(id)
    }

    fun setPin(id: Int, pin: String?) {
        val allChannels = if (channels.value != null) channels.value else ArrayList()
        for (channel in allChannels!!) {
            if (channel.id == id) {
                channel.pin = pin
            }
        }
    }

    fun savePins(c: Context) {
        val selectedChannels = if (channels.value != null) channels.value else ArrayList()
        for (channel in selectedChannels!!) {
            if (channel.pin != null && channel.pin.isNotEmpty()) {
                channel.pin = KeyStoreExecutor.createNewKey(channel.pin, c)
                repo.update(channel)
            }
        }
    }

    fun savePin(channel: Channel, c: Context) {
        if (channel.pin != null && channel.pin.isNotEmpty()) {
            channel.pin = KeyStoreExecutor.createNewKey(channel.pin, c)
            repo.update(channel)
        }
    }

    fun clearAllPins() {
        val selectedChannels = if (channels.value != null) channels.value else ArrayList()
        for (channel in selectedChannels!!) {
            channel.pin = null
            repo.update(channel)
        }
    }

    fun removeChannel(channel: Channel) {
        val channelDefaultChanged = channel.defaultAccount
        channel.selected = false
        channel.defaultAccount = false
        repo.update(channel)

        removeAccounts(channel.id)

        if (channels.value != null && channelDefaultChanged) {
            setRandomChannelAsDefault(channel.id)
        }
    }

    private fun removeAccounts(channelId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val accounts = repo.getAccounts(channelId)
        accounts.forEach { repo.delete(it) }
    }

    private fun setRandomChannelAsDefault(excludedChannelId: Int) {
        for (c in channels.value!!) {
            if (c.id != excludedChannelId) {
                c.defaultAccount = true
                repo.update(c)
                return
            }
        }
    }

    fun setDefaultChannel(channel: Channel) {
        if (channels.value == null) return
        for (c in channels.value!!) {
            c.defaultAccount = c.id == channel.id
            repo.update(c)
        }
    }

}