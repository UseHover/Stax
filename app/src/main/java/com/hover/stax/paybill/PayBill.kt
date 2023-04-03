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
package com.hover.stax.paybill

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hover.sdk.actions.HoverAction
import com.hover.stax.domain.model.Account
import com.hover.stax.database.channel.entity.Channel
import javax.annotation.Nullable

const val BUSINESS_NO = "businessNo"
const val BUSINESS_NAME = "businessName"

@Entity(
    tableName = "paybills",
    foreignKeys = [
        ForeignKey(entity = Channel::class, parentColumns = ["id"], childColumns = ["channelId"]),
        ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["accountId"])
    ],
    indices = [Index(value = ["business_no", "account_no"], unique = true)]
)
data class Paybill(

    var name: String,

    @ColumnInfo(name = "business_name")
    var businessName: String?,

    @ColumnInfo(name = "business_no")
    var businessNo: String?,

    @ColumnInfo(name = "account_no")
    var accountNo: String? = null,

    @Nullable
    @ColumnInfo(name = "action_id", defaultValue = "")
    var actionId: String? = null,

    @ColumnInfo(index = true)
    val accountId: Int = 0,

    @ColumnInfo(name = "logo_url")
    val logoUrl: String
) : Comparable<Paybill> {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo(name = "recurring_amount")
    var recurringAmount: Int = 0

    @ColumnInfo(index = true)
    var channelId: Int = 0

    var logo: Int = 0

    @ColumnInfo(defaultValue = "0")
    var isSaved: Boolean = false

    override fun toString() = buildString {
        append(name)
        append(" (")
        append(businessNo)
        append(")")
    }

    // FIXME: is this actually used?
    override fun equals(other: Any?): Boolean {
        if (other !is Paybill) return false
        return id == other.id || (channelId == other.channelId && businessNo == other.businessNo) // FIXME: should be institution id not channel id
    }

    override fun compareTo(other: Paybill): Int = toString().compareTo(other.toString())

    companion object {
        fun extractBizNumber(action: HoverAction): String {
            return if (action.getVarValue(BUSINESS_NO) != null)
                action.getVarValue(BUSINESS_NO)
            else ""
        }
    }
}