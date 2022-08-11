package com.hover.stax.hover

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.transactions.TransactionContract
import com.hover.stax.domain.model.ACCOUNT_ID
import com.hover.stax.domain.model.Account
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.channels.Channel
import com.hover.stax.data.local.channels.ChannelRepo
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.StaxContact
import com.hover.stax.domain.model.PLACEHOLDER
import com.hover.stax.merchants.MerchantRepo
import com.hover.stax.paybill.BUSINESS_NAME
import com.hover.stax.paybill.BUSINESS_NO
import com.hover.stax.paybill.PaybillRepo
import com.hover.stax.requests.RequestRepo
import com.hover.stax.transactions.TransactionRepo
import com.hover.stax.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.regex.Pattern

class TransactionReceiver : BroadcastReceiver(), KoinComponent {

    private val repo: TransactionRepo by inject()
    private val actionRepo: ActionRepo by inject()
    private val channelRepo: ChannelRepo by inject()
    private val accountRepo: AccountRepo by inject()
    private val contactRepo: ContactRepo by inject()
    private val billRepo: PaybillRepo by inject()
    private val merchantRepo: MerchantRepo by inject()
    private val requestRepo: RequestRepo by inject()

    private var channel: Channel? = null
    private var account: Account? = null
    private var action: HoverAction? = null
    private var contact: StaxContact? = null

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val actionId = intent.getStringExtra(TransactionContract.COLUMN_ACTION_ID)

                actionId?.let {
                    action = actionRepo.getAction(it)

                    //added null check to prevent npe whenever action is null
                    action?.let { a ->
                        channel = channelRepo.getChannel(a.channel_id)

                        createAccounts(intent)
                        updateBalance(intent)
                        updateContacts(intent)
                        updateTransaction(intent, context.applicationContext)
                        updateBusinesses(intent)
                        updateRequests(intent)
                    }
                }
            }
        }
    }

    private fun updateBalance(intent: Intent) {
        if (intent.hasExtra(TransactionContract.COLUMN_INPUT_EXTRAS)) {
            val inputExtras = intent.getSerializableExtra(TransactionContract.COLUMN_INPUT_EXTRAS) as HashMap<String, String>

            if (inputExtras.containsKey(ACCOUNT_ID)) {
                val accountId = inputExtras[ACCOUNT_ID]
                accountId?.let {
                    account = accountRepo.getAccount(accountId.toInt())
                    Timber.e("$account")
                }
            }
        }

        if (intent.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES)) {
            val parsedVariables = intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as HashMap<String, String>

            if (account != null && parsedVariables.containsKey("balance")) {
                account!!.updateBalance(parsedVariables)
                accountRepo.update(account!!)
            }
        }
    }

    private fun updateContacts(intent: Intent) {
        contact = StaxContact.findOrInit(intent, channel!!.countryAlpha2, contactRepo)
        contact?.let {
            it.updateNames(intent)
            contactRepo.save(it)
        }
    }

    private fun updateBusinesses(intent: Intent) {
        if (intent.getStringExtra(TransactionContract.COLUMN_TYPE) == HoverAction.BILL && getBizNo(intent) != null) {
            val bill = billRepo.getMatching(getBizNo(intent)!!, channel!!.id)
            if (bill != null && bill.businessName.isNullOrEmpty() && getBizName(intent) != null) {
                bill.businessName = getBizName(intent)
                billRepo.save(bill)
            }
        } else if (intent.getStringExtra(TransactionContract.COLUMN_TYPE) == HoverAction.MERCHANT && getBizNo(intent) != null) {
            val merchant = merchantRepo.getMatching(getBizNo(intent)!!, channel!!.id)
            if (merchant != null && merchant.businessName.isNullOrEmpty() && getBizName(intent) != null) {
                merchant.businessName = getBizName(intent)
                merchantRepo.save(merchant)
            }
        }
    }

    private fun getBizNo(intent: Intent): String? {
        val inExtras = intent.getSerializableExtra(TransactionContract.COLUMN_INPUT_EXTRAS) as java.util.HashMap<String, String>?
        if (inExtras != null && inExtras.containsKey(BUSINESS_NO))
            return inExtras[BUSINESS_NO]
        else return null
    }

    private fun getBizName(intent: Intent): String? {
        val outExtras = intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as java.util.HashMap<String, String>?
        if (outExtras != null && outExtras.containsKey(BUSINESS_NAME))
            return outExtras[BUSINESS_NAME]?.replace(".", "") // MPESA adds a gramatically incorrect period which isn't easily fixable with a regex
        else return null
    }

    private fun updateTransaction(intent: Intent, c: Context) {
        repo.insertOrUpdateTransaction(intent, action!!, contact!!, c)
    }

    private fun updateRequests(intent: Intent) {
        if (intent.getStringExtra(TransactionContract.COLUMN_TYPE) == HoverAction.RECEIVE) {
            requestRepo.requests.forEach {
                if (it.requestee_ids.contains(contact!!.id) && Utils.getAmount(
                        it.amount
                            ?: "00"
                    ) == Utils.getAmount(getAmount(intent)!!)
                ) {
                    it.matched_transaction_uuid = intent.getStringExtra(TransactionContract.COLUMN_UUID)
                    requestRepo.update(it)
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
        if (intent.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES)) {
            val parsedVariables = intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as HashMap<String, String>

            if (parsedVariables.containsKey("userAccountList")) {
                val accounts = accountRepo.getAllAccounts().toMutableList()
                val parsedAccounts = parseAccounts(parsedVariables["userAccountList"]!!)

                removePlaceholders(parsedAccounts, accounts.map { it.channelId }.toIntArray())

                accountRepo.saveAccounts(parseAccounts(parsedVariables["userAccountList"]!!))
            }
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

        if (accountRepo.getDefaultAccount() == null && accounts.isNotEmpty())
            accounts.first().isDefault = true

        return accounts
    }

    private fun removePlaceholders(parsedAccounts: List<Account>, savedAccounts: IntArray) {
        parsedAccounts.forEach {
            if (savedAccounts.contains(it.channelId)) {
                Timber.e("Removing ${it.channelId} from ${it.name}")
                accountRepo.deleteAccount(it.channelId, it.name.plus(PLACEHOLDER))
            }
        }
    }
}