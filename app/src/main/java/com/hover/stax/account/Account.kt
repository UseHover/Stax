package com.hover.stax.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import com.hover.stax.channels.Channel

@Entity(
    tableName = "accounts",
    foreignKeys = [ForeignKey(entity = Channel::class, parentColumns = ["id"], childColumns = ["channelId"], onDelete = CASCADE)]
)
data class Account(
    val name: String,

    val alias: String,

    @ColumnInfo(name = "logo_url")
    val logoUrl: String,

    @ColumnInfo(name = "account_no")
    val accountNo: String,

    @ColumnInfo(index = true)
    val channelId: Int
) : Comparable<Account> {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var latestBalance: String? = null

    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    var latestBalanceTimestamp: Long = System.currentTimeMillis()

    override fun toString() = buildString { append(name); append(" "); append(accountNo) }

    override fun equals(other: Any?): Boolean {
        if (other !is Account) return false
        return id == other.id
    }

    override fun compareTo(other: Account): Int = toString().compareTo(other.toString())
}