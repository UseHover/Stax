package com.hover.stax.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.hover.stax.domain.model.StaxUser

/**
 * Response returned from the server when a user logs in or updates their details.
 */
data class StaxUserDto(

	@SerializedName("data")
	val data: Data
)

data class Data(

	@SerializedName("attributes")
	val attributes: Attributes,

	@SerializedName("id")
	val id: String,

	@SerializedName("type")
	val type: String
)

data class Attributes(

	@SerializedName("bounty_total")
	val bountyTotal: Int,

	@SerializedName("referee_id")
	val refereeId: Int,

	@SerializedName("devices")
	val devices: List<String>,

	@SerializedName("transaction_count")
	val transactionCount: Int,

	@SerializedName("created_at")
	val createdAt: String,

	@SerializedName("id")
	val id: Int,

	@SerializedName("is_verified_mapper")
	val isVerifiedMapper: Boolean,

	@SerializedName("email")
	val email: String,

	@SerializedName("username")
	val username: String,

	@SerializedName("marketing_opted_in")
	val marketingOptedIn: Boolean 
)

/**
 * Mapper to convert a [StaxUserDto] to a [StaxUser].
 */
fun StaxUserDto.toStaxUser(): StaxUser {
	return StaxUser(
		id = data.attributes.id,
		username = data.attributes.username,
		email = data.attributes.email,
		isMapper = data.attributes.isVerifiedMapper,
		marketingOptedIn = data.attributes.marketingOptedIn,
		transactionCount = data.attributes.transactionCount,
		bountyTotal = data.attributes.bountyTotal
	)
}