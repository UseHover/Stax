package com.hover.stax.transactions;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.transactions.Transaction;

import java.util.ArrayList;
import java.util.List;

public class UssdCallResponse {
    String enteredValue, responseMessage;

    public UssdCallResponse(String sent, String response) {
        enteredValue = sent != null ? sent : "";
        if (enteredValue.equals("(pin)")) {
            this.enteredValue = "****";
        }
        responseMessage = response != null ? response : "";
    }

    static List<UssdCallResponse> generateConvo(Transaction t, HoverAction a) {
        ArrayList<UssdCallResponse> convo = new ArrayList<>();
        int i = 0;
        while (i == 0 || (t.enteredValues != null && t.enteredValues.opt(i - 1) != null) || (t.ussdMessages != null && t.ussdMessages.opt(i) != null)) {

            UssdCallResponse tm;
            if (i == 0 && !t.myType.equals(HoverAction.RECEIVE))
                tm = new UssdCallResponse(a.root_code, t.ussdMessages != null ? t.ussdMessages.optString(i) : null);
            else
                tm = new UssdCallResponse(t.enteredValues != null ? t.enteredValues.optString(i - 1) : null,
                        t.ussdMessages != null ? t.ussdMessages.optString(i) : null);
            convo.add(tm);
            i++;
        }
        return convo;
    }
}
