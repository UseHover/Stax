package com.hover.stax.presentation.add_accounts.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.hover.stax.channels.Channel
import com.hover.stax.domain.model.*

class SampleAccountProvider : PreviewParameterProvider<List<Account>> {
    override val values: Sequence<List<Account>> = sequenceOf(
        listOf(
            USSDAccount("MPESA", "MPESA", "", "070555555", -1, USSD_TYPE, "#FFFFFF", "#000000", Channel.MOBILE_MONEY, "ke", -1, true),
            USSDAccount("TKash", "TKash", "", null, -1, USSD_TYPE, "#FFFFFF", "#000000", Channel.MOBILE_MONEY, "ke", -1, false),
            USSDAccount("Absa Bank", "Absa Bank", "", "0123456789", -1, USSD_TYPE, "#FFFFFF", "#000000", Channel.BANK_TYPE, "ke", -1, false),
            USDCAccount(
                "Stellar USDC", "Stellar USDC", "", "ABCDEF0123456789", -1, CRYPTO_TYPE, "Stellar color 1", "stellar color 2",
                "native", null, false
            )
        )
    )
}