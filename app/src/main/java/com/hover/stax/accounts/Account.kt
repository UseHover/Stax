package com.hover.stax.accounts

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.hover.stax.channels.Channel
import com.hover.stax.utils.DateUtils.now
import timber.log.Timber

const val DUMMY = -1

@Entity(
        tableName = "accounts",
        foreignKeys = [ForeignKey(entity = Channel::class, parentColumns = ["id"], childColumns = ["channelId"], onDelete = CASCADE)],
        indices = [Index(value = ["name"], unique = true)]
)
data class Account(
        val name: String,

        var alias: String,

        @ColumnInfo(name = "logo_url")
        val logoUrl: String,

        @ColumnInfo(name = "account_no")
        var accountNo: String?,

        @ColumnInfo(index = true)
        val channelId: Int,

        @ColumnInfo(name = "primary_color_hex")
        val primaryColorHex: String,

        @ColumnInfo(name = "secondary_color_hex")
        val secondaryColorHex: String,

        @ColumnInfo(defaultValue = "0")
        var isDefault: Boolean = false
) : Comparable<Account> {

    constructor(name: String, channel: Channel) : this(
            name, name, channel.logoUrl, "", channel.id, channel.primaryColorHex, channel.secondaryColorHex
    )

    constructor(name: String, primaryColor: String) : this(
            name, alias = name, logoUrl = "", accountNo = "", channelId = -1, primaryColor, secondaryColorHex = "#1E232A"
    )

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var latestBalance: String? = null

    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    var latestBalanceTimestamp: Long = System.currentTimeMillis()

    fun updateBalance(parsed_variables: HashMap<String, String>) {
        if (parsed_variables.containsKey("balance")) latestBalance = parsed_variables["balance"]

        Timber.e("Balance is $latestBalance")

        latestBalanceTimestamp = if (parsed_variables.containsKey("update_timestamp") && parsed_variables["update_timestamp"] != null) {
            parsed_variables["update_timestamp"]!!.toLong()
        } else {
            now()
        }
    }

    fun dummy(): Account {
        id = DUMMY
        latestBalanceTimestamp = -1L
        latestBalance = "0"
        return this
    }

    override fun toString() = buildString {
        append(alias)

        if (accountNo != null) {
            append(" - ")
            append(accountNo)
        }
    }

    //    Name is unique
    override fun equals(other: Any?): Boolean {
        if (other !is Account) return false
        return id == other.id || other.name == other.name
    }

    override fun compareTo(other: Account): Int = toString().compareTo(other.toString())
}