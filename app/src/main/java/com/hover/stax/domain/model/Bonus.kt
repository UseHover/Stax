package com.hover.stax.domain.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.firebase.firestore.QueryDocumentSnapshot

@Entity(
    tableName = "bonuses",
    primaryKeys = ["user_channel", "purchase_channel"]
)
data class Bonus(

    @ColumnInfo(name = "user_channel")
    val userChannel: Int,

    @ColumnInfo(name = "bonus_percent")
    val bonusPercent: Double,

    val message: String,

    @ColumnInfo(name = "hni_list", defaultValue = "0")
    @NonNull
    val hniList: String,

    @Deprecated("hniList is now being used to filter which bonus is being displayed," +
            " but it might have a usefulness later")
    @ColumnInfo(name = "purchase_channel")
    val purchaseChannel: Int,
) {
    override fun toString(): String {
        return message
    }

    constructor(document: QueryDocumentSnapshot) : this(
        document.data["user_channel"].toString().toInt(),
        document.data["bonus_percent"].toString().toDouble(),
        document.data["message"].toString(),
        document.data["hniList"].toString(), document.data["purchase_channel"].toString().toInt())
}

data class BonusList(val bonuses: List<Bonus> = emptyList())