package com.hover.stax.paybill

import androidx.room.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.accounts.Account
import com.hover.stax.channels.Channel

@Entity(
        tableName = "paybills",
        foreignKeys = [ForeignKey(entity = Channel::class, parentColumns = ["id"], childColumns = ["channelId"]),
            ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["accountId"])],
        indices = [Index(value = ["business_no", "account_no"], unique = true)]
)
data class Paybill(

        var name: String,

        @ColumnInfo(name = "business_no")
        var businessNo: String,

        @ColumnInfo(name = "account_no")
        var accountNo: String? = null,

        @ColumnInfo(index = true)
        val channelId: Int,

        @ColumnInfo(index = true)
        val accountId: Int,

        @ColumnInfo(name = "logo_url")
        val logoUrl: String
) : Comparable<Paybill> {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo(name = "recurring_amount")
    var recurringAmount: Int = 0

    var logo: Int = 0

    @ColumnInfo(defaultValue = "0")
    var isSaved: Boolean = false

    override fun toString() = buildString {
        append(name)
        append(" (")
        append(businessNo)
        append(")")
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Account) return false
        return id == other.id || other.name == other.name
    }

    override fun compareTo(other: Paybill): Int = toString().compareTo(other.toString())
}