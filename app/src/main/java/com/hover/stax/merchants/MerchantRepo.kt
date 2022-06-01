package com.hover.stax.merchants

import androidx.lifecycle.LiveData
import com.hover.stax.database.AppDatabase
import kotlinx.coroutines.flow.Flow

class MerchantRepo(db: AppDatabase) {

	private val merchantDao: MerchantDao = db.merchantDao()

	val all: LiveData<List<Merchant>> = merchantDao.all

	fun get(id: Int): Merchant? = merchantDao.getMerchant(id)

	fun getMatching(tillNo: String, channelId: Int): Merchant? = merchantDao.getMerchantsByNo(tillNo, channelId)

	fun getLiveMatching(tillNo: String, channelId: Int): LiveData<Merchant?> = merchantDao.getLiveMerchantsByNo(tillNo, channelId)

	fun save(merchant: Merchant) = merchantDao.insert(merchant)

	fun update(merchant: Merchant) = merchantDao.update(merchant)

	fun delete(merchant: Merchant) = merchantDao.delete(merchant)
}