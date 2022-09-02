package com.hover.stax.data.remote.dto

import com.hover.stax.domain.model.Bonus


data class BonusDto (
	val userChannel: Int,
	val bonusPercent: Double,
	val message: String,
	val hniList: String,
	val purchaseChannel: Int)

fun BonusDto.toStaxBonus() : Bonus {
	return Bonus(
		this.userChannel,
		this.bonusPercent,
		this.message,
		this.hniList,
		this.purchaseChannel
	)
}