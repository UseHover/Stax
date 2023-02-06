package com.hover.stax.presentation.add_account.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.hover.stax.channels.Channel

class SampleChannelProvider : PreviewParameterProvider<List<Channel>> {
	override val values: Sequence<List<Channel>> = sequenceOf(listOf(
		Channel(0, "MPESA", "ke", "*334#", "KSH", "[32001]", "logo", 0, Channel.MOBILE_MONEY, "#FFFFFF", "#000000", true),
		Channel(1, "TKash", "ke", "*888#", "KSH", "[32003]", "logo", 1, Channel.MOBILE_MONEY, "#FFFFFF", "#000000", true),
		Channel(2, "Absa Bank", "ke", "*123#", "KSH", "[32001]", "logo", 2, Channel.BANK_TYPE, "#FFFFFF", "#000000", true),
		Channel(3, "Zenith Bank", "ke", "*222#", "KSH", "[32001]", "logo", 3, Channel.BANK_TYPE, "#FFFFFF", "#000000", true),
	))
}