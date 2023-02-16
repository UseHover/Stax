/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.domain.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hover.stax.channels.Channel
import com.hover.stax.utils.DateUtils.now
import timber.log.Timber

@Entity(
    tableName = "ussd_accounts",
    foreignKeys = [ForeignKey(entity = Channel::class, parentColumns = ["id"], childColumns = ["channelId"])],
    indices = [Index(value = ["institutionAccountName", "sim_subscription_id"], unique = true)]
)
class USSDAccount(institutionName : String, userAlias: String, logoUrl: String, accountNo: String?, institutionId: Int, type: String, primaryColorHex: String, secondaryColorHex: String,

    @NonNull
    @ColumnInfo(name = "institution_type", defaultValue = Channel.BANK_TYPE)
    var institutionType: String,

    @JvmField
    @ColumnInfo
    var countryAlpha2: String,

    @ColumnInfo(index = true)
    var channelId: Int,

    @ColumnInfo(defaultValue = "0")
    var isDefault: Boolean = false,

    @NonNull
    @ColumnInfo(name = "sim_subscription_id", defaultValue = "-1")
    var simSubscriptionId: Int = -1

) : Account(institutionName, userAlias, logoUrl, accountNo, institutionId, type, primaryColorHex, secondaryColorHex) {

    constructor(name: String) : this(name, name)
    constructor(name: String, alias: String) : this(
        institutionName = name, userAlias = alias, logoUrl = "", accountNo = "", institutionId = -1, type = "", primaryColorHex = "#292E35", secondaryColorHex = "#1E232A",
        institutionType = "", countryAlpha2 = "", channelId = -1
    )

    constructor(channel: Channel, isDefault: Boolean, simSubscriptionId: Int) : this(
        channel.name, channel, isDefault, simSubscriptionId
    )
    constructor(alias: String, channel: Channel, isDefault: Boolean, simSubscriptionId: Int) : this(
        channel.name, alias, channel.logoUrl, "", channel.institutionId, USSD_TYPE, channel.primaryColorHex, channel.secondaryColorHex, channel.institutionType, channel.countryAlpha2, channel.id, isDefault, simSubscriptionId
    )

    fun updateBalance(parsed_variables: HashMap<String, String>) {
        if (parsed_variables.containsKey("balance")) latestBalance = parsed_variables["balance"]

        Timber.e("Balance is $latestBalance")

        latestBalanceTimestamp = if (parsed_variables.containsKey("update_timestamp") && parsed_variables["update_timestamp"] != null) {
            parsed_variables["update_timestamp"]!!.toLong()
        } else {
            now()
        }
    }

    fun getAccountNameExtra(): String {
        return institutionAccountName ?: "1"
    }

    override fun toString() = buildString {
        append(userAlias)

        if (!accountNo.isNullOrEmpty()) {
            append(" - ")
            append(accountNo)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is USSDAccount) return false
        return id == other.id ||
            (institutionName == other.institutionName && simSubscriptionId == other.simSubscriptionId && institutionAccountName == other.institutionAccountName)
    }
}