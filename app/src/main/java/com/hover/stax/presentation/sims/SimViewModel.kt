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
package com.hover.stax.presentation.sims

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hover.sdk.api.Hover
import com.hover.stax.domain.use_case.ListSimsUseCase
import com.hover.stax.domain.use_case.SimWithAccount
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class SimViewModel(private val listSimsUseCase: ListSimsUseCase, val application: Application) : ViewModel() {

    private val _sims = MutableStateFlow<List<SimWithAccount>>(emptyList())
    val sims = _sims.asStateFlow()
    private var simReceiver: BroadcastReceiver? = null

    init {
        simReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                fetchSims()
            }
        }

        loadSims()
    }

    private fun loadSims() {
        fetchSims()

        simReceiver?.let {
            LocalBroadcastManager.getInstance(application)
                .registerReceiver(it, IntentFilter(Utils.getPackage(application) + ".NEW_SIM_INFO_ACTION"))
        }
        Hover.updateSimInfo(application)
    }

    private fun fetchSims() = viewModelScope.launch(Dispatchers.IO) {
        Timber.e("Loading sims")
        _sims.update { listSimsUseCase() }
    }
}