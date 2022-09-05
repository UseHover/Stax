package com.hover.stax.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "token_info",
)
data class TokenInfo(

    @PrimaryKey
    val id: Int = 1,

    val accessToken: String,

    val refreshToken: String,

    val scope: String,

    val createdAt: Int,

    val tokenType: String,

    val expiresIn: Int
)