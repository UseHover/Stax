package com.hover.stax.storage.channel.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

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
    val isFavorite: Boolean = false
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