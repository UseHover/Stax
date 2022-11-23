package com.hover.stax.merchants

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hover.stax.data.local.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantDao : BaseDao<Merchant> {

	@get:Query("SELECT * FROM merchants ORDER BY last_used_timestamp ASC")
	val all: LiveData<List<Merchant>>

	@Query("SELECT * FROM merchants WHERE till_no = :till AND channelId = :channelId LIMIT 1")
	fun getMerchantsByNo(till: String, channelId: Int): Merchant?

	@Query("SELECT * FROM merchants WHERE till_no = :till AND channelId = :channelId LIMIT 1")
	fun getLiveMerchantsByNo(till: String, channelId: Int): LiveData<Merchant?>

	@Query("SELECT * FROM merchants WHERE id = :id LIMIT 1")
	fun getMerchant(id: Int): Merchant?

	@Query("DELETE FROM merchants")
	fun deleteAll()
}