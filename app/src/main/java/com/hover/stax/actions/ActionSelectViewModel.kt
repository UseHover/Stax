package com.hover.stax.actions

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.actions.HoverAction.*
import com.hover.stax.R
import com.hover.stax.utils.Constants
import java.util.LinkedHashMap

 class ActionSelectViewModel(private val application: Application) : ViewModel() {

    private val filteredActions = MediatorLiveData<List<HoverAction>>()
    val activeAction = MediatorLiveData<HoverAction>()
    val nonStandardVariables = MediatorLiveData<LinkedHashMap<String, String>>()

    init {
        activeAction.addSource(filteredActions, this::setActiveActionIfOutOfDate)
        nonStandardVariables.addSource(activeAction, this::initNonStandardVariables)
    }

    private fun setActiveActionIfOutOfDate(actions: List<HoverAction>) {
        if (!actions.isNullOrEmpty() && (activeAction.value == null || !actions.contains(activeAction.value!!))) {
            val action = actions.first()
            activeAction.postValue(action)
        }
    }

    fun setActions(actions: List<HoverAction>) = filteredActions.postValue(actions)

    fun setActiveAction(action: HoverAction?) = action?.let { activeAction.postValue(it) }

    fun errorCheck(): String? {
        return if (activeAction.value == null) application.getString(R.string.action_fielderror) else null
    }

    private fun initNonStandardVariables(action: HoverAction) {
        val variableMap = LinkedHashMap<String, String>()
        action.requiredParams.forEach {
            if (!isStandardVariable(it)) variableMap[it] = ""
        }
        nonStandardVariables.postValue(variableMap)
    }

    private fun isStandardVariable(key: String): Boolean {
        return key in listOf(PHONE_KEY, ACCOUNT_KEY, AMOUNT_KEY, NOTE_KEY, PIN_KEY, Constants.RECIPIENT_INSTITUTION)
    }

    fun updateNonStandardVariables(key: String, value: String) {
        var map = nonStandardVariables.value
        if (map == null) map = linkedMapOf<String, String>()
        map[key] = value
        nonStandardVariables.postValue(map!!);
    }
}