/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.futureTransactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.requests.Request
import com.hover.stax.requests.RequestRepo
import com.hover.stax.schedules.Schedule
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.storage.channel.entity.Channel
import com.hover.stax.storage.channel.repository.ChannelRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class FutureViewModel(
    private val repo: RequestRepo,
    private val channelRepository: ChannelRepository,
    private val scheduleRepo: ScheduleRepo
) : ViewModel() {

    var scheduled: LiveData<List<Schedule>> = MutableLiveData()
    var requests: LiveData<List<Request>> = MutableLiveData()

    init {
        scheduled = scheduleRepo.futureTransactions
        requests = repo.liveRequests
    }

    fun requestsByChannel(channelId: Int): LiveData<List<Request>> {
        val channel = runBlocking {
            getChannelAsync(channelId).await()
        }

        return repo.getLiveRequests(channel.institutionId)
    }

    fun scheduledByChannel(channelId: Int): LiveData<List<Schedule>> = scheduleRepo.getFutureTransactions(channelId)

    private fun getChannelAsync(channelId: Int): Deferred<Channel> = viewModelScope.async(Dispatchers.IO) {
        channelRepository.getChannel(channelId)!!
    }
}