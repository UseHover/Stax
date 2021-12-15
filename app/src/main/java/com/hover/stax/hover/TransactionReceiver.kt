package com.hover.stax.hover

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.transactions.TransactionContract
import com.hover.stax.accounts.Account
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.utils.Constants
import com.hover.stax.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.regex.Pattern

class TransactionReceiver : BroadcastReceiver(), KoinComponent {

    private val repo: DatabaseRepo by inject()

    private var channel: Channel? = null
    private var account: Account? = null
    private var action: HoverAction? = null
    private var contact: StaxContact? = null

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val actionId = intent.getStringExtra(TransactionContract.COLUMN_ACTION_ID)

                actionId?.let {
                    action = repo.getAction(it)
                    channel = repo.getChannel(action!!.channel_id)

                    createAccounts(intent)
                    updateBalance(intent)
                    updateContacts(intent)
                    updateTransaction(intent, context.applicationContext)
                    updateRequests(intent)
                }
            }
        }
    }

    private fun updateBalance(intent: Intent) {
        if (intent.hasExtra(TransactionContract.COLUMN_INPUT_EXTRAS)) {
            val inputExtras = intent.getSerializableExtra(TransactionContract.COLUMN_INPUT_EXTRAS) as HashMap<String, String>

            if (inputExtras.containsKey(Constants.ACCOUNT_ID)) {
                val accountId = inputExtras[Constants.ACCOUNT_ID]
                accountId?.let {
                    account = repo.getAccount(accountId.toInt())
                    Timber.e("$account")
                }
            }
        }

        if (intent.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES)) {
            val parsedVariables = intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as HashMap<String, String>

            if (account != null && parsedVariables.containsKey("balance")) {
                account!!.updateBalance(parsedVariables)
                repo.update(account!!)
            }
        }
    }

    private fun updateContacts(intent: Intent) {
        contact = StaxContact.findOrInit(intent, channel!!.countryAlpha2, repo)
        contact!!.updateNames(intent)
        repo.save(contact!!)
    }

    private fun updateTransaction(intent: Intent, c: Context) {
        repo.insertOrUpdateTransaction(intent, action!!, contact!!, c)
    }

    private fun updateRequests(intent: Intent) {
        if (intent.getStringExtra(TransactionContract.COLUMN_TYPE) == HoverAction.RECEIVE) {
            repo.requests.forEach {
                if (it.requestee_ids.contains(contact!!.id) && Utils.getAmount(it.amount ?: "00") == Utils.getAmount(getAmount(intent)!!)) {
                    it.matched_transaction_uuid = intent.getStringExtra(TransactionContract.COLUMN_UUID)
                    repo.update(it)
                }
            }
        }
    }

    private fun getAmount(intent: Intent): String? = when {
        intent.hasExtra(TransactionContract.COLUMN_INPUT_EXTRAS) ->
            getAmount(intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as? HashMap<String, String>)
        intent.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES) ->
            getAmount(intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as? HashMap<String, String>)
        else -> null
    }

    private fun getAmount(extras: HashMap<String, String>?): String? = if (extras != null && extras.containsKey(HoverAction.AMOUNT_KEY))
        extras[HoverAction.AMOUNT_KEY]
    else null

    private fun createAccounts(intent: Intent) {
        val accounts = repo.getAllAccounts().toMutableList()

        if (intent.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES)) {
            val parsedVariables = intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as HashMap<String, String>

            if (parsedVariables.containsKey("userAccountList")) {
                accounts.addAll(parseAccounts(parsedVariables["userAccountList"]!!))
            }

            repo.saveAccounts(accounts)
        }
    }

    private fun parseAccounts(accountList: String): List<Account> {
        val pattern = Pattern.compile("^[\\d]{1,2}[>):.\\s]+(.+)\$", Pattern.MULTILINE)
        val matcher = pattern.matcher(accountList)

        val accounts = ArrayList<Account>()
        while (matcher.find()) {
            val newAccount = Account(matcher.group(1)!!, channel!!)
            accounts.add(newAccount)
        }

        if (repo.getDefaultAccount() == null && accounts.isNotEmpty())
            accounts.first().isDefault = true

        return accounts
    }
}