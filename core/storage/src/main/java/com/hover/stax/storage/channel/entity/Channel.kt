package com.hover.stax.storage.channel.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey(autoGenerate = false)
    val id: Int,

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
    val isFavorite: Boolean = false
)