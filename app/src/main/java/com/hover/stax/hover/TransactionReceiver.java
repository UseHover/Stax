package com.hover.stax.hover;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;

import java.util.HashMap;

import static org.koin.java.KoinJavaComponent.get;

public class TransactionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseRepo repo = get(DatabaseRepo.class);
        updateBalance(repo, intent);
        updateTransaction(repo, intent, context);
    }

    private void updateBalance(DatabaseRepo repo, Intent intent) {
        if (intent.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES)) {
            HashMap<String, String> parsed_variables = (HashMap<String, String>) intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES);
            if (parsed_variables != null && parsed_variables.containsKey("balance")) {
                new Thread(() -> {
                    HoverAction action = repo.getAction(intent.getStringExtra(TransactionContract.COLUMN_ACTION_ID));
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
