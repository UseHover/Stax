package com.hover.stax.storage.channel.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

private enum class ChannelTypes(val type:String){
    BANK(type = "bank"),
    TELECOM(type = "telecom"),
    MOBILE_MONEY(type = "mmo")
}


@Entity(tableName = "channels")
data class ChannelNow(
    @PrimaryKey(autoGenerate = false) val id:Int
)