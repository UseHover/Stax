package com.hover.stax.user

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

    val transactionCount: Int,

    val bountyTotal: Int
)
