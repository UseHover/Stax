package com.hover.stax.domain.model

import androidx.annotation.NonNull
import androidx.room.*
import com.hover.stax.channels.Channel
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.DateUtils.now
import timber.log.Timber
import kotlin.random.Random

const val PLACEHOLDER = " placeholder"
const val ACCOUNT_NAME: String = "account_name"
const val ACCOUNT_ID: String = "account_id"

@Entity(
        tableName = "accounts",
        foreignKeys = [ForeignKey(entity = Channel::class, parentColumns = ["id"], childColumns = ["channelId"])],
        indices = [Index(value = ["name"], unique = true)]
)
data class Account(
        val name: String,

        var alias: String,

        @ColumnInfo(name = "logo_url")
        val logoUrl: String,

        @ColumnInfo(name = "account_no")
        var accountNo: String?,

        @ColumnInfo
        var institutionId: Int?,

        @NonNull
        @ColumnInfo(name = "institution_type", defaultValue = Channel.BANK_TYPE)
        var institutionType: String,

        @JvmField
        @ColumnInfo
        var countryAlpha2: String?,

        @ColumnInfo(index = true)
        var channelId: Int,

        @ColumnInfo(name = "primary_color_hex")
        val primaryColorHex: String,

        @ColumnInfo(name = "secondary_color_hex")
        val secondaryColorHex: String,

        @ColumnInfo(defaultValue = "0")
        var isDefault: Boolean = false,

        @NonNull
        @ColumnInfo(name = "sim_subscription_id", defaultValue = "-1")
        var simSubscriptionId: Int = -1

) : Comparable<Account> {

    constructor(name: String, channel: Channel, isDefault: Boolean, simSubscriptionId: Int) : this(
        name, name, channel.logoUrl, "", channel.institutionId, channel.institutionType, channel.countryAlpha2, channel.id, channel.primaryColorHex, channel.secondaryColorHex, isDefault, simSubscriptionId
    )

    constructor(name: String) : this(name, primaryColor = "#292E35")

    constructor(name: String, primaryColor: String) : this(
            name, alias = name, logoUrl = "", accountNo = "", institutionId = -1, institutionType = "", countryAlpha2 = "", channelId = -1, primaryColor, secondaryColorHex = "#1E232A"
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

    override fun toString() = buildString {
        append(alias)

        if (!accountNo.isNullOrEmpty()) {
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

    companion object {
        fun generateDummy(name: String? = "Dummy account", accountId: Int? = -1) : Account {
            return Account(name!!).apply {
                id = accountId!!
                simSubscriptionId = Random(2).nextInt()
                latestBalance = "Not yet checked"
                latestBalanceTimestamp = now()
            }
        }
    }
}