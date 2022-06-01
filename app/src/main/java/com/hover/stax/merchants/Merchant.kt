package com.hover.stax.merchants

import androidx.room.*
import com.hover.stax.accounts.Account
import com.hover.stax.channels.Channel
import javax.annotation.Nullable

@Entity(tableName = "merchants",
	foreignKeys = [ForeignKey(entity = Channel::class, parentColumns = ["id"], childColumns = ["channelId"])],
	indices = [Index(value = ["business_no"], unique = true)]
)
data class Merchant(

	@ColumnInfo(name = "business_name")
	var businessName: String?,

	@ColumnInfo(name = "business_no")
	var businessNo: String,

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
		append(businessName)
		append(" (")
		append(businessNo)
		append(")")
	}

	override fun equals(other: Any?): Boolean {
		if (other !is Account) return false
		return id == other.id || other.name == other.name
	}

	override fun compareTo(other: Merchant): Int = businessNo.compareTo(other.businessNo)

	fun shortName(): String? {
		return if (hasName()) businessName else businessNo
	}

	fun hasName(): Boolean {
		return businessName != null && !businessName!!.isEmpty()
	}
}