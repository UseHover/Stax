package com.hover.stax.actions

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.actions.HoverAction.*
import com.hover.stax.R
import com.hover.stax.utils.Constants
import timber.log.Timber
import java.util.LinkedHashMap

 class ActionSelectViewModel(private val application: Application) : ViewModel() {

    val filteredActions = MediatorLiveData<List<HoverAction>>()
    val activeAction = MediatorLiveData<HoverAction>()
    val nonStandardVariables = MediatorLiveData<LinkedHashMap<String, String>>()

    init {
        activeAction.addSource(filteredActions, this::setActiveActionIfOutOfDate)
        nonStandardVariables.addSource(activeAction, this::initNonStandardVariables)
    }

    private fun setActiveActionIfOutOfDate(actions: List<HoverAction>) {
        if (actions.isNotEmpty() && (activeAction.value == null || !actions.contains(activeAction.value!!))) {
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
            Timber.e("Required param - $it")
            if (!isStandardVariable(it)) variableMap[it] = ""
        }
        nonStandardVariables.postValue(variableMap)
    }

    private fun isStandardVariable(key: String): Boolean {
        return key in listOf(PHONE_KEY, ACCOUNT_KEY, AMOUNT_KEY, NOTE_KEY, PIN_KEY, Constants.RECIPIENT_INSTITUTION, Constants.ACCOUNT_NAME)
    }

    fun updateNonStandardVariables(key: String, value: String) {
        val map = nonStandardVariables.value ?: linkedMapOf()
        map[key] = value
        nonStandardVariables.postValue(map)
    }
}