package com.hover.stax.bills

import com.hover.stax.database.AppDatabase
import kotlinx.coroutines.flow.Flow

class PaybillRepo(db: AppDatabase) {

    private val paybillDao: PaybillDao = db.paybillDao()

    val allBills: Flow<List<Paybill>> = paybillDao.allBills

    fun getPaybills(accountId: Int): Flow<List<Paybill>> = paybillDao.getBillsByAccount(accountId)
}