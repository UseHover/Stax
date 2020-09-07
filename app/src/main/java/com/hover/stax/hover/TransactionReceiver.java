package com.hover.stax.hover;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hover.stax.database.DatabaseRepo;

public class TransactionReceiver extends BroadcastReceiver {
	final private static String TAG = "TransactionReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "Recieved broadcast");
		DatabaseRepo repo = new DatabaseRepo((Application) context.getApplicationContext());

	}
}
