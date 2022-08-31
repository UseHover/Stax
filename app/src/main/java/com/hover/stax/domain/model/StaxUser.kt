package com.hover.stax.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "stax_users",
)
data class StaxUser(

    @PrimaryKey
    val id: Int,

    val username: String,

    val email: String,

    @ColumnInfo(defaultValue = "0")
    val isMapper: Boolean,

    @ColumnInfo(defaultValue = "0")
    val marketingOptedIn: Boolean,

    val transactionCount: Int,

    val bountyTotal: Int,

    @ColumnInfo(defaultValue = "0")
    val totalPoints: Int
)
