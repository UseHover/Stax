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

class TransactionReceiver : BroadcastReceiver(), KoinComponent, PushNotificationTopicsInterface {

    private val repo: DatabaseRepo by inject()

    override fun onReceive(context: Context, intent: Intent) {
        updateBalance(intent, context)
        updateTransaction(intent, context)
    }

    private fun updateBalance(intent: Intent, context: Context) {
        Timber.e("${intent.extras?.get(TransactionContract.COLUMN_PARSED_VARIABLES)}")

        if (intent.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES)) {
            val parsedVariables = intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as? HashMap<String, String>

            parsedVariables?.let { variables ->
                GlobalScope.launch(Dispatchers.IO) {
                    val action = repo.getAction(intent.getStringExtra(TransactionContract.COLUMN_ACTION_ID))
                    val channel = repo.getChannel(action.channel_id)

                    if (variables.containsKey("userAccountList")) {
                        setChannelSelected(channel, context)
                    } else if (variables.containsKey("balance")) {
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

    private fun setChannelSelected(channel: Channel, context: Context) {
        logChoice(channel, context)
        channel.selected = true

        if (repo.getSelectedCount() == 0)
            channel.defaultAccount = true

        repo.update(channel)
    }

    private fun logChoice(channel: Channel, context: Context) {
        joinChannelGroup(channel.id, context)
        val args = JSONObject()

        try {
            args.put(context.getString(R.string.added_channel_id), channel.id)
        } catch (ignored: Exception) {
        }

        Utils.logAnalyticsEvent(context.getString(R.string.new_channel_selected), args, context)
    }
}