package com.hover.stax.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import com.hover.stax.channels.Channel
import com.hover.stax.utils.DateUtils.now
import java.util.HashMap

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
    var accountNo: String?,

    @ColumnInfo(index = true)
    val channelId: Int
) : Comparable<Account> {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var latestBalance: String? = null

    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    var latestBalanceTimestamp: Long = System.currentTimeMillis()

    fun updateBalance(parsed_variables: HashMap<String, String>) {
        if (parsed_variables.containsKey("balance")) latestBalance = parsed_variables["balance"]

        latestBalanceTimestamp = if (parsed_variables.containsKey("update_timestamp") && parsed_variables["update_timestamp"] != null) {
            parsed_variables["update_timestamp"]!!.toLong()
        } else {
            now()
        }
    }

    override fun toString() = buildString { append(name); append(" "); append(accountNo) }

    override fun equals(other: Any?): Boolean {
        if (other !is Account) return false
        return id == other.id
    }

    override fun compareTo(other: Account): Int = toString().compareTo(other.toString())
}