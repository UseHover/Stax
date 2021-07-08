package com.hover.stax.actions

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R

class ActionSelectViewModel(private val application: Application) : ViewModel() {

    private val filteredActions = MediatorLiveData<List<HoverAction>>()
    val activeAction = MediatorLiveData<HoverAction>()

    init {
        activeAction.addSource(filteredActions, this::setActiveActionIfOutOfDate)
    }

    private fun setActiveActionIfOutOfDate(actions: List<HoverAction>) {
        if (!actions.isNullOrEmpty() && (activeAction.value == null || !actions.contains(activeAction.value!!))) {
            val action = actions.first()
            activeAction.postValue(action)
        }
    }

    fun setActions(actions: List<HoverAction>) = filteredActions.postValue(actions)

    fun setActiveAction(action: HoverAction?) = action?.let { activeAction.postValue(action) }

    fun errorCheck(): String? {
        return if (activeAction.value == null) application.getString(R.string.action_fielderror) else null
    }
}