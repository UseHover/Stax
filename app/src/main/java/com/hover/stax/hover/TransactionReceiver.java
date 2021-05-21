package com.hover.stax.hover;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;

import java.util.HashMap;

public class TransactionReceiver extends BroadcastReceiver {
    final private static String TAG = "TransactionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseRepo repo = new DatabaseRepo((Application) context.getApplicationContext());
        updateBalance(repo, intent);
        updateTransaction(repo, intent, context);
    }

    private void updateBalance(DatabaseRepo repo, Intent intent) {
        if (intent.hasExtra("parsed_variables")) {
            HashMap<String, String> parsed_variables = (HashMap<String, String>) intent.getSerializableExtra("parsed_variables");
            if (parsed_variables != null && parsed_variables.containsKey("balance")) {
                new Thread(() -> {
                    HoverAction action = repo.getAction(intent.getStringExtra("action_id"));
                    Channel channel = repo.getChannel(action.channel_id);
                    channel.updateBalance(parsed_variables);
                    repo.update(channel);
                }).start();
            }
        }
    }

    private void updateTransaction(DatabaseRepo repo, final Intent intent, Context c) {
        repo.insertOrUpdateTransaction(intent, c);
    }
}
