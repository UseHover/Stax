package com.hover.stax.hover

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.transactions.TransactionContract
import com.hover.stax.R
import com.hover.stax.account.Account
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
import java.util.regex.Matcher
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
                    if (variables.containsKey("userAccountList")) {
                        val accountList = variables["userAccountList"]
                        parseOutAccounts(accountList!!).forEachIndexed { index, s -> Timber.e("$index - $s") }
                    }
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

    fun parseOutAccounts(fullString: String): List<String> {

        fun getRawAccounts(): String {
            val m: Matcher = Pattern.compile("([\\d]{1,2})[\\>)\\:\\.\\s]+(.+)$").matcher(fullString);
            val accounts = StringBuilder();
            while (m.find()) {
                accounts.append(m.group(0))
                Timber.i("Found: %s, with size %s", m.group(0), m.groupCount());
            }
            return accounts.toString()
        }

        fun getAccountAsList(): List<String> {
            val p = Pattern.compile("([\\d]{1,2})([.-:])(\\s)");
            return fullString.split(p);
        }

        fun validAccounts(): List<String> {
            return getAccountAsList().filter { s: String -> s.length > 2 }
        }

        return validAccounts()
    }

}