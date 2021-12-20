package com.hover.stax.bills

import androidx.room.*
import com.hover.stax.accounts.Account
import com.hover.stax.channels.Channel

@Entity(
        tableName = "bills",
        foreignKeys = [ForeignKey(entity = Channel::class, parentColumns = ["id"], childColumns = ["channelId"]),
            ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["accountId"])],
        indices = [Index(value = ["business_no", "account_no"], unique = true)]
)
data class Bill(

        val name: String,

        @ColumnInfo(name = "business_no")
        val businessNo: String,

        @ColumnInfo(name = "account_no")
        var accountNo: String? = null,

        var logo: Int = 0,

        @ColumnInfo(index = true)
        val channelId: Int,

        @ColumnInfo(index = true)
        val accountId: Int,

        @ColumnInfo(defaultValue = "0")
        var isSaved: Boolean = false

) : Comparable<Bill> {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo(name = "recurring_amount")
    var recurringAmount: Int = 0

    override fun toString() = buildString {
        append(name)
        append(" - ")
        append(businessNo)

        accountNo?.let {
            append(" - ")
            append(it)
        }
    }

    //    Name is unique
    override fun equals(other: Any?): Boolean {
        if (other !is Account) return false
        return id == other.id || other.name == other.name
    }

    override fun compareTo(other: Bill): Int = toString().compareTo(other.toString())
}