package com.hover.stax.hover

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hover.sdk.transactions.TransactionContract
import com.hover.stax.database.DatabaseRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class TransactionReceiver : BroadcastReceiver(), KoinComponent {

    private val repo: DatabaseRepo by inject()

    override fun onReceive(context: Context, intent: Intent) {
        updateBalance(repo, intent)
        updateTransaction(repo, intent, context)
    }

    private fun updateBalance(repo: DatabaseRepo, intent: Intent) {
        Timber.e("${intent.extras?.get(TransactionContract.COLUMN_PARSED_VARIABLES)}")

        if (intent.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES)) {
            val parsedVariables = intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as? HashMap<String, String>

            parsedVariables?.entries?.forEach {
                Timber.e("Entries - $it")
            }

            if (parsedVariables != null && parsedVariables.containsKey("balance")) {
                GlobalScope.launch(Dispatchers.IO) {
                    val action = repo.getAction(intent.getStringExtra(TransactionContract.COLUMN_ACTION_ID))
//                    val channel = repo.getChannel(action.channel_id)

                    val account = repo.getAccounts(action.channel_id).first()
                    account.updateBalance(parsedVariables)
//                    channel.updateBalance(parsedVariables)
//                    repo.update(channel)
                    repo.update(account)
                }
            }
        }
    }

    private fun updateTransaction(repo: DatabaseRepo, intent: Intent, c: Context) {
        repo.insertOrUpdateTransaction(intent, c)
    }
}