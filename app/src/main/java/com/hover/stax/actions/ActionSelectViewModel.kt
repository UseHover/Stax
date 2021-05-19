package com.hover.stax.actions

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import timber.log.Timber

class ActionSelectViewModel(private val application: Application) : ViewModel() {

    val filteredActions = MediatorLiveData<List<HoverAction>>()
    val activeAction = MediatorLiveData<HoverAction>()

    init {
        activeAction.addSource(filteredActions, this::setActiveActionIfOutOfDate)
    }

    private fun setActiveActionIfOutOfDate(actions: List<HoverAction>) {
        Timber.e("May be setting active action")

        if (!actions.isNullOrEmpty() && (activeAction.value == null || !actions.contains(activeAction.value!!))) {
            val action = actions.first()

            Timber.e("Auto selecting $action ${action.transaction_type} ${action.recipientInstitutionId()} ${action.public_id}")

            activeAction.postValue(action)
        }
    }

    fun setActions(actions: List<HoverAction>) = filteredActions.postValue(actions)

    fun setActiveAction(action: HoverAction?) = action?.let { activeAction.postValue(action) }

    fun errorCheck(): String? {
        return if (activeAction.value == null) application.getString(R.string.action_fielderror) else null
    }
}