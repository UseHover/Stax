package com.hover.stax.merchants

import androidx.room.*
import com.hover.stax.domain.model.Account
import com.hover.stax.channels.Channel
import javax.annotation.Nullable

@Entity(tableName = "merchants",
	foreignKeys = [ForeignKey(entity = Channel::class, parentColumns = ["id"], childColumns = ["channelId"])]
)
data class Merchant(

	@ColumnInfo(name = "business_name")
	var businessName: String?,

	@ColumnInfo(name = "till_no")
	var tillNo: String,

	@Nullable
	@ColumnInfo(name = "action_id", defaultValue = "")
	var actionId: String? = null,

	@ColumnInfo(index = true)
	val accountId: Int = 0,

	@ColumnInfo(index = true)
	var channelId: Int = 0

) : Comparable<Merchant> {

	@PrimaryKey(autoGenerate = true)
	var id: Int = 0

	@ColumnInfo(name = "last_used_timestamp")
	var lastUsedTimestamp: Long? = null

	override fun toString() = buildString {
		if (!businessName.isNullOrEmpty()) {
			append(businessName)
			append(" (")
		}
		append(tillNo)
		if (!businessName.isNullOrEmpty())
			append(")")
	}

	override fun equals(other: Any?): Boolean {
		if (other !is Account) return false
		return id == other.id || other.name == other.name
	}

	override fun compareTo(other: Merchant): Int = tillNo.compareTo(other.tillNo)

	fun shortName(): String? {
		return if (hasName()) businessName else tillNo
	}

	fun hasName(): Boolean {
		return businessName != null && !businessName!!.isEmpty()
	}
}