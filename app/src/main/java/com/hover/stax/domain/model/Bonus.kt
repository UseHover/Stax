package com.hover.stax.domain.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot

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

    val message: String,

    @ColumnInfo(name = "hni_list", defaultValue = "0")
    @NonNull
    val hniList: String,
) {
    override fun toString(): String {
        return message
    }

    constructor(document: DocumentSnapshot) : this(
        document["user_channel"].toString().toInt(),
        document["purchase_channel"].toString().toInt(),
        document["bonus_percent"].toString().toDouble(),
        document["message"].toString(),
        document["hniList"].toString())
}

data class BonusList(val bonuses: List<Bonus> = emptyList())