/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.hover

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.transactions.Transaction
import com.hover.sdk.transactions.TransactionContract
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.StaxContact
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.data.local.channels.ChannelRepo
import com.hover.stax.domain.model.ACCOUNT_ID
import com.hover.stax.domain.model.Account
import com.hover.stax.merchants.MerchantRepo
import com.hover.stax.paybill.BUSINESS_NAME
import com.hover.stax.paybill.BUSINESS_NO
import com.hover.stax.paybill.PaybillRepo
import com.hover.stax.requests.RequestRepo
import com.hover.stax.transactions.TransactionRepo
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Utils
import java.util.regex.Pattern
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

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
        if (intent != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val actionId = intent.getStringExtra(TransactionContract.COLUMN_ACTION_ID)
                val type = intent.getStringExtra(TransactionContract.COLUMN_TYPE)!!

                if (actionId != null && type != Transaction.VAR_CHECK) {
                    action = actionRepo.getAction(actionId)

                    // added null check to prevent npe whenever action is null
                    action?.let { a ->
                        channel = channelRepo.getChannel(a.channel_id)

                        updateContacts(intent)
                        updateTransaction(intent, context.applicationContext)
                        updateBalance(intent)
                        updateAccounts(intent)
                        updateBusinesses(intent)
                        updateRequests(intent)
                    }
                } else if (actionId == null) {
                    AnalyticsUtil.logAnalyticsEvent("TransactionReceiver received event with no action ID", context)
                }
            }
        } else {
            AnalyticsUtil.logAnalyticsEvent("TransactionReceiver received event with no intent", context)
        }
    }

    private suspend fun updateBalance(intent: Intent) {
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
        return if (inExtras != null && inExtras.containsKey(BUSINESS_NO))
            inExtras[BUSINESS_NO]
        else null
    }

    private fun getBizName(intent: Intent): String? {
        val outExtras = intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as java.util.HashMap<String, String>?
        return if (outExtras != null && outExtras.containsKey(BUSINESS_NAME))
            outExtras[BUSINESS_NAME]?.replace(".", "") // MPESA adds a gramatically incorrect period which isn't easily fixable with a regex
        else null
    }

    private fun updateTransaction(intent: Intent, c: Context) {
        repo.insertOrUpdateTransaction(intent, action!!, contact!!, c)
    }

    private fun updateRequests(intent: Intent) {
        if (intent.getStringExtra(TransactionContract.COLUMN_TYPE) == HoverAction.RECEIVE) {
            requestRepo.requests.forEach {
                if (it.requestee_ids.contains(contact!!.id) && Utils.amountToDouble(it.amount) == Utils.amountToDouble(getAmount(intent)!!)
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

    private suspend fun updateAccounts(intent: Intent) {
        if (intent.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES)) {
            val parsedVariables = intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as HashMap<String, String>

            if (parsedVariables.containsKey("userAccountList")) {
                parseAccounts(parsedVariables["userAccountList"]!!)
            }
        }
    }

    private suspend fun parseAccounts(ussdAccountList: String) {
        val pattern = Pattern.compile("^([\\d]{1,2})[>):.\\s]+(.+)\$", Pattern.MULTILINE)
        val matcher = pattern.matcher(ussdAccountList)

        while (matcher.find()) {
            try {
                val accounts = accountRepo.getAccountsByChannel(channel!!.id)
                if (accounts.any { it.institutionAccountName == matcher.group(2)!! }) { break }

                val a = if (account != null && account!!.institutionAccountName == null) {
                    account!!
                } else if (accounts.any { it.institutionAccountName == null }) {
                    accounts.first { it.institutionAccountName == null }
                } else {
                    Account(matcher.group(2)!!, channel!!, false, account?.simSubscriptionId ?: -1)
                }

                a.institutionAccountName = matcher.group(2)!!
                if (a.institutionName == a.userAlias) a.userAlias = matcher.group(2)!!

                accountRepo.saveAccount(a)
            } catch (e: Exception) { AnalyticsUtil.logErrorAndReportToFirebase(TransactionReceiver::class.java.simpleName, "Failed to parse account list from USSD", e) }
        }
    }
}