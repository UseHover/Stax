package com.hover.stax.merchants

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantDao {
	@get:Query("SELECT * FROM merchants ORDER BY business_no ASC")
	val all: LiveData<List<Merchant>>

	@Query("SELECT * FROM merchants WHERE business_no LIKE :businessNo ORDER BY business_no ASC")
	fun getMerchantsByNo(businessNo: String): Flow<List<Merchant>>

	@Query("SELECT * FROM merchants WHERE id = :id ORDER BY business_no ASC")
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