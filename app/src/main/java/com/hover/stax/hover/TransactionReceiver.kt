package com.hover.stax.hover

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hover.sdk.transactions.TransactionContract
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.pushNotification.PushNotificationTopicsInterface
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.regex.Pattern

class TransactionReceiver : BroadcastReceiver(), KoinComponent, PushNotificationTopicsInterface {

    private val repo: DatabaseRepo by inject()

    override fun onReceive(context: Context, intent: Intent) {
        updateBalance(intent, context)
        updateTransaction(intent, context)
    }

    private fun updateBalance(intent: Intent, context: Context) {
        if (intent.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES)) {
            val parsedVariables = intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as? HashMap<String, String>

            parsedVariables?.let { variables ->
                GlobalScope.launch(Dispatchers.IO) {
                    if (variables.containsKey("balance")) {
                        val action = repo.getAction(intent.getStringExtra(TransactionContract.COLUMN_ACTION_ID))
                        val account = repo.getAccounts(action.channel_id).first()
                        account.updateBalance(parsedVariables)
                        repo.update(account)
                    }
                }
            }
        }
    }

    private fun updateTransaction(intent: Intent, c: Context) {
        repo.insertOrUpdateTransaction(intent, c)
    }
}