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
package com.hover.stax.merchants

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.hover.stax.channels.Channel
import javax.annotation.Nullable

@Entity(
    tableName = "merchants",
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

// 	FIXME: Is this actually used?
    override fun equals(other: Any?): Boolean {
        if (other !is Merchant) return false
        return id == other.id || (channelId == other.channelId && tillNo == tillNo)
    }

    override fun compareTo(other: Merchant): Int = tillNo.compareTo(other.tillNo)

    fun shortName(): String? {
        return if (hasName()) businessName else tillNo
    }

    fun hasName(): Boolean {
        return businessName != null && businessName!!.isNotEmpty()
    }
}