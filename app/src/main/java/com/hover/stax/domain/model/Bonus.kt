package com.hover.stax.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "bonuses",
    primaryKeys = ["user_channel", "purchase_channel"]
)
data class Bonus(

    @ColumnInfo(name = "user_channel")
    val userChannel: Int,

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