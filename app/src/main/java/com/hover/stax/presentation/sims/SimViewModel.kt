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
import com.hover.stax.domain.use_case.sims.SimWithAccount
import com.hover.stax.domain.use_case.sims.ListSimsUseCase
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class SimViewModel(private val listSimsUseCase: ListSimsUseCase, val application: Application) : ViewModel() {

    private val _sims = MutableStateFlow<List<SimWithAccount>>(emptyList())
    val sims = _sims.asStateFlow()

    var loading = true

    private var simReceiver: BroadcastReceiver? = null

    init {
        simReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                fetchSims()
            }
        }

        loadSims()
//        fetchBonuses()
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
        loading = false
    }

//    private fun fetchBonuses() = viewModelScope.launch {
//        bonusUseCase.bonusList.collect { bonusList ->
////            _simUiState.update { it.copy(bonuses = bonusList) }
//        }
//    }
}