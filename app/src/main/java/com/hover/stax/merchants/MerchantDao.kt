package com.hover.stax.merchants

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantDao {
	@get:Query("SELECT * FROM merchants ORDER BY till_no ASC")
	val all: LiveData<List<Merchant>>

	@Query("SELECT * FROM merchants WHERE till_no = :till AND channelId = :channelId ORDER BY till_no ASC")
	fun getMerchantsByNo(till: String, channelId: Int): Merchant?

	@Query("SELECT * FROM merchants WHERE till_no = :till AND channelId = :channelId ORDER BY till_no ASC")
	fun getLiveMerchantsByNo(till: String, channelId: Int): LiveData<Merchant?>

	@Query("SELECT * FROM merchants WHERE id = :id ORDER BY till_no ASC")
	fun getMerchant(id: Int): Merchant?

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	fun insert(vararg merchant: Merchant?)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(merchant: Merchant)

	@Update
	fun update(merchant: Merchant)

	@Update
	fun update(merchant: List<Merchant>)

	@Delete
	fun delete(merchant: Merchant)

	@Query("DELETE FROM merchants")
	fun deleteAll()
}