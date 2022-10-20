package com.hover.stax.presentation.sims.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.hover.sdk.mocks.MockSimInfo
import com.hover.sdk.sims.SimInfo

class SampleSimInfoProvider: PreviewParameterProvider<SimInfo> {
	override val values: Sequence<SimInfo> = sequenceOf(
		MockSimInfo(1, 0, "6350111010101", "63501", "0984234325", "0703535226", "Safaricom", "ke", false),
		MockSimInfo(2, 1, "6350222222222", "63502", null, null, "Airtel", "ke", false),
		MockSimInfo(3, -1, "6340122222222", "63401", null, null, null, "ng", false)
	)
}