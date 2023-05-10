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
package com.hover.stax.actions

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.actions.HoverAction.ACCOUNT_KEY
import com.hover.sdk.actions.HoverAction.AMOUNT_KEY
import com.hover.sdk.actions.HoverAction.NOTE_KEY
import com.hover.sdk.actions.HoverAction.PHONE_KEY
import com.hover.sdk.actions.HoverAction.PIN_KEY
import com.hover.stax.R
import com.hover.stax.database.models.ACCOUNT_NAME

const val RECIPIENT_INSTITUTION = "recipientInstitution"

@Suppress("WildcardImport")
class ActionSelectViewModel(application: Application) : AndroidViewModel(application) {

    val filteredActions = MutableLiveData<List<HoverAction>>()
    val activeAction = MediatorLiveData<HoverAction?>()
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

    fun setActiveAction(actionId: String?) = actionId?.let { setActiveAction(filteredActions.value?.find { it.public_id == actionId }) }

    fun setActiveAction(action: HoverAction?) = action?.let { activeAction.postValue(it) }

    fun errorCheck(): String? {
        return if (activeAction.value == null) (getApplication() as Context).getString(R.string.action_fielderror) else null
    }

    private fun initNonStandardVariables(action: HoverAction?) {
        action?.let {
            val variableMap = LinkedHashMap<String, String>()
            val keys: Iterator<String> = action.required_params.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (!isStandardVariable(key)) variableMap[key] = ""
            }
            nonStandardVariables.postValue(variableMap)
        }
    }

    private fun isStandardVariable(key: String): Boolean {
        return key in listOf(PHONE_KEY, ACCOUNT_KEY, AMOUNT_KEY, NOTE_KEY, PIN_KEY, RECIPIENT_INSTITUTION, ACCOUNT_NAME)
    }

    fun updateNonStandardVariables(key: String, value: String) {
        val map = nonStandardVariables.value ?: linkedMapOf()
        map[key] = value
        nonStandardVariables.postValue(map)
    }

    fun wrapExtras(): HashMap<String, String> {
        return nonStandardVariables.value ?: hashMapOf()
    }
}