package com.hover.stax.requests;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;

import com.hover.sdk.sms.IncomingSMSReceiver;

public class SmsReceiver extends BroadcastReceiver {
	private final static String TAG = "SmsReceiver";

	public SmsReceiver() {}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			SmsMessage[] messages = IncomingSMSReceiver.getMessages(intent);
			if (messages != null && messages.length > 0)
				processSMS(context, messages);
		} catch (Exception e) {
			Log.d(TAG, "Error processing SMS", e);
		}
	}

	private void processSMS(Context context, SmsMessage[] messages) throws Exception {
		for (SmsMessage m : messages) {
			String phoneNumber = m != null ? m.getDisplayOriginatingAddress() : "";
			String message = m.getDisplayMessageBody();

			if (message.contains("stax.me")) {
				Log.e(TAG, "Got a stax request!");
			}
		}
	}
}
