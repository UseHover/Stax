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
package com.hover.stax.transactionDetails

import com.hover.sdk.actions.HoverAction
import com.hover.sdk.transactions.Transaction
import java.util.ArrayList

class UssdCallResponse(sent: String?, response: String?) {
    var enteredValue: String
    var responseMessage: String
    var isShortCode = false

    constructor(sent: String?, response: String?, isShortCode: Boolean) : this(sent, response) {
        this.isShortCode = isShortCode
    }

    companion object {
        fun generateConvo(t: Transaction, a: HoverAction): List<UssdCallResponse> {
            val convo = ArrayList<UssdCallResponse>()
            var i = 0
            while (i == 0 || t.enteredValues != null && t.enteredValues.opt(i - 1) != null || t.ussdMessages != null && t.ussdMessages.opt(
                    i
                ) != null
            ) {
                val tm: UssdCallResponse = if (i == 0 && t.myType != HoverAction.RECEIVE) UssdCallResponse(
                    a.root_code,
                    if (t.ussdMessages != null) t.ussdMessages.optString(i) else null,
                    true
                ) else UssdCallResponse(
                    if (t.enteredValues != null) t.enteredValues.optString(i - 1) else null,
                    if (t.ussdMessages != null) t.ussdMessages.optString(i) else null
                )
                convo.add(tm)
                i++
            }
            return convo
        }
    }

    init {
        enteredValue = sent ?: ""
        if (enteredValue == "(pin)") {
            enteredValue = "****"
        }
        responseMessage = response ?: ""
    }
}