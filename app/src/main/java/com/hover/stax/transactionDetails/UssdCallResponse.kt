package com.hover.stax.transactionDetails

import com.hover.sdk.actions.HoverAction
import com.hover.sdk.transactions.Transaction
import com.hover.stax.transactionDetails.UssdCallResponse
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
				var tm: UssdCallResponse
				tm = if (i == 0 && t.myType != HoverAction.RECEIVE) UssdCallResponse(
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