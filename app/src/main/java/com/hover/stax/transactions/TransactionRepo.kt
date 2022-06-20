package com.hover.stax.transactions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.transactions.TransactionContract
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.database.AppDatabase
import com.hover.stax.domain.model.Account
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class TransactionRepo(db: AppDatabase) {
    private val transactionDao: TransactionDao = db.transactionDao()

    val completeAndPendingTransferTransactions: LiveData<List<StaxTransaction>>?
        get() = transactionDao.getCompleteAndPendingTransfers()

    val bountyTransactions: LiveData<List<StaxTransaction>>?
        get() = transactionDao.bountyTransactions

    val transactionsForAppReview: LiveData<List<StaxTransaction>>?
        get() = transactionDao.transactionsForAppReview

    val allNonBountyTransactions: LiveData<List<StaxTransaction>>
        get() = transactionDao.nonBountyTransactions

    @SuppressLint("DefaultLocale")
    suspend fun hasTransactionLastMonth(): Boolean {
        return transactionDao.getTransactionCount(String.format("%02d", DateUtils.lastMonth().first), DateUtils.lastMonth().second.toString())!! > 0
    }

    fun getAccountTransactions(account: Account): LiveData<List<StaxTransaction>>? {
        return transactionDao.getAccountTransactions(account.id)
    }

    @SuppressLint("DefaultLocale")
    fun getSpentAmount(accountId: Int, month: Int, year: Int): LiveData<Double>? {
        return transactionDao.getTotalAmount(accountId, String.format("%02d", month), year.toString())
    }

    @SuppressLint("DefaultLocale")
    fun getFees(accountId: Int, year: Int): LiveData<Double>? {
        return transactionDao.getTotalFees(accountId, year.toString())
    }

    private fun getTransaction(uuid: String?): StaxTransaction? {
        return transactionDao.getTransaction(uuid)
    }

    fun getTransactionAsync(uuid: String): Flow<StaxTransaction> = transactionDao.getTransactionAsync(uuid)

    fun insertOrUpdateTransaction(intent: Intent, action: HoverAction, contact: StaxContact, c: Context) {
        AppDatabase.databaseWriteExecutor.execute {
            try {
                var t = getTransaction(intent.getStringExtra(TransactionContract.COLUMN_UUID))
                Timber.e("Found t uuid: ${t?.uuid}")
                if (t == null) {
                    AnalyticsUtil.logAnalyticsEvent(c.getString(R.string.transaction_started), c, true)
                    t = StaxTransaction(intent, action, contact, c)
                    transactionDao.insert(t)
                    t = transactionDao.getTransaction(t.uuid)
                } else {
                    AnalyticsUtil.logAnalyticsEvent(c.getString(R.string.transaction_completed), c, true)
                    t.update(intent, action, contact, c)
                    transactionDao.update(t)
                }
                Timber.e("save t with uuid: %s", t?.uuid)
            } catch (e: Exception) {
                Timber.e(e, "error")
            }
        }
    }
}