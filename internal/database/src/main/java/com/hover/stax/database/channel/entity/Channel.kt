/*
 * Copyright 2023 Stax
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
package com.hover.stax.database.channel.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "country_alpha2")
    val countryAlpha2: String,

    @ColumnInfo(name = "root_code")
    val rootCode: String,

    @ColumnInfo(name = "currency")
    val currency: String,

    @ColumnInfo(name = "hni_list")
    val hniList: String,

    @ColumnInfo(name = "logo_url")
    val logoUrl: String,

    @ColumnInfo(name = "institution_id")
    val institutionId: Int,

    @ColumnInfo(name = "primary_color_hex")
    val primaryColorHex: String,

    @ColumnInfo(name = "published")
    val published: Boolean = false,

    @ColumnInfo(name = "secondary_color_hex")
    val secondaryColorHex: String,

    @ColumnInfo(name = "institution_type")
    val institutionType: String = ChannelTypes.BANK.type,

    @ColumnInfo(name = "isFavorite")
    var isFavorite: Boolean = false
) : Comparable<Channel> {

    override fun equals(other: Any?): Boolean {
        if (other !is Channel) return false
        return id == other.id
    }

    val ussdName: String
        get() = name + " - " + rootCode + " - " + countryAlpha2.uppercase(Locale.getDefault())

    override fun toString(): String {
        return name + " " + countryAlpha2.uppercase(Locale.getDefault())
    }

    override fun compareTo(other: Channel): Int {
        return this.toString().compareTo(other.toString())
    }
}