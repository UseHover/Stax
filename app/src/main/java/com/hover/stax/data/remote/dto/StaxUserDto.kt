/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.data.remote.dto

import com.hover.stax.storage.user.entity.StaxUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response returned from the server when a user logs in or updates their details.
 */
@Serializable
data class StaxUserDto(
    @SerialName("data")
    val data: Data
)

@Serializable
data class Data(
    @SerialName("attributes")
    val attributes: Attributes,
    @SerialName("id")
    val id: String,
    @SerialName("type")
    val type: String
)

@Serializable
data class Attributes(
    @SerialName("bounty_total")
    val bountyTotal: Int,
    @SerialName("referee_id")
    val refereeId: Int?,
    @SerialName("devices")
    val devices: List<String>,
    @SerialName("transaction_count")
    val transactionCount: Int,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("id")
    val id: Int,
    @SerialName("is_verified_mapper")
    val isVerifiedMapper: Boolean,
    @SerialName("email")
    val email: String,
    @SerialName("username")
    val username: String,
    @SerialName("marketing_opted_in")
    val marketingOptedIn: Boolean,
    @SerialName("total_points")
    val totalPoints: Int
)

/**
 * Mapper to convert a [StaxUserDto] to a [StaxUser].
 */
fun StaxUserDto.toStaxUser(): com.hover.stax.storage.user.entity.StaxUser {
    return com.hover.stax.storage.user.entity.StaxUser(
        id = data.attributes.id,
        username = data.attributes.username,
        email = data.attributes.email,
        isMapper = data.attributes.isVerifiedMapper,
        marketingOptedIn = data.attributes.marketingOptedIn,
        transactionCount = data.attributes.transactionCount,
        bountyTotal = data.attributes.bountyTotal,
        totalPoints = data.attributes.totalPoints,
    )
}