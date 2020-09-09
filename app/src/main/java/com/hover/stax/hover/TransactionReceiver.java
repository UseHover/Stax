package com.hover.stax.hover;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hover.sdk.utils.Utils;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;

import java.util.HashMap;

public class TransactionReceiver extends BroadcastReceiver {
	final private static String TAG = "TransactionReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		DatabaseRepo repo = new DatabaseRepo((Application) context.getApplicationContext());
		if (intent.hasExtra("parsed_variables")) {
			HashMap<String, String> parsed_variables = (HashMap<String, String>) intent.getSerializableExtra("parsed_variables");
			if (parsed_variables.containsKey("balance")) {
				new Thread(new Runnable() {
					public void run() {
						Action action = repo.getAction(intent.getStringExtra("action_id"));
						Channel channel = repo.getChannel(action.channel_id);
						channel.latestBalance = parsed_variables.get("balance");
						if (parsed_variables.containsKey("update_timestamp") && parsed_variables.get("update_timestamp") != null) {
							channel.latestBalanceTimestamp = Long.parseLong(parsed_variables.get("update_timestamp"));
						} else {
							channel.latestBalanceTimestamp = Utils.now();
						}
						repo.update(channel);
					}
				}).start();
			}
		}
	}
}
