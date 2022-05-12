package com.hover.stax.bonus

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "bonuses",
    primaryKeys = ["recipient_channel", "purchase_channel"]
)
data class Bonus(

    @ColumnInfo(name = "recipient_channel")
    val recipientChannel: Int,

    @ColumnInfo(name = "purchase_channel")
    val purchaseChannel: Int,

    @ColumnInfo(name = "bonus_percent")
    val bonusPercent: Double,

    val message: String
) {
    override fun toString(): String {
        return message
    }
}