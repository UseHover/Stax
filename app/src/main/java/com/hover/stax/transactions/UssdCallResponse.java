package com.hover.stax.transactions;

import android.util.Log;

import com.hover.sdk.transactions.Transaction;
import com.hover.stax.actions.Action;

import java.util.ArrayList;
import java.util.List;

public class UssdCallResponse {
	String enteredValue, responseMessage;

	public UssdCallResponse(String sent, String response) {
		enteredValue = sent != null ? sent : "";
		if (enteredValue.equals("(pin)")) { this.enteredValue = "****"; }
		responseMessage = response != null ? response : "";
	}

	static List<UssdCallResponse> generateConvo(Transaction t, Action a) {
		ArrayList<UssdCallResponse> convo = new ArrayList<>();
		int i = 0;
		while (i == 0 || t.enteredValues.opt(i - 1) != null || t.ussdMessages.opt(i) != null) {

			UssdCallResponse tm = null;
			if (i == 0)
				 tm = new UssdCallResponse(a.root_code, t.ussdMessages.optString(i));
			else tm = new UssdCallResponse(t.enteredValues.optString(i - 1), t.ussdMessages.optString(i));
			convo.add(tm);
			i++;
		}
		return convo;
	}
}
