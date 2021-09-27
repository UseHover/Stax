package com.hover.stax.hover

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hover.sdk.transactions.TransactionContract
import com.hover.stax.account.Account
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.pushNotification.PushNotificationTopicsInterface
import com.hover.stax.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class TransactionReceiver : BroadcastReceiver(), KoinComponent, PushNotificationTopicsInterface {

    private val repo: DatabaseRepo by inject()

    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            updateBalance(intent, context)
        }
    }

    private fun updateBalance(intent: Intent, context: Context) {
        var account: Account? = null

        if(intent.hasExtra(TransactionContract.COLUMN_INPUT_EXTRAS)){
            val inputExtras = intent.getSerializableExtra(TransactionContract.COLUMN_INPUT_EXTRAS) as? HashMap<String, String>

            if(inputExtras!!.containsKey(Constants.ACCOUNT_NAME)){
                val accountName = inputExtras[Constants.ACCOUNT_NAME]
                account = repo.getAccount(accountName!!)
            }
        }

        Timber.e("Account - ${account?.toString()}")

        if (intent.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES)) {
            val parsedVariables = intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as? HashMap<String, String>

            parsedVariables?.let { variables ->
                if (variables.containsKey("balance")) {
                    account!!.updateBalance(parsedVariables)
                    repo.update(account)
                }
            }
        }

        repo.insertOrUpdateTransaction(intent, context)
    }
}