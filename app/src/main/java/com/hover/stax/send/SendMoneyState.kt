package com.hover.stax.send

import com.hover.stax.domain.model.Account

data class SendMoneyState(
    val fromAccount: Account? = null,
    val toAccount: Account? = null,
    val recipientAccount: String? = null,
    val amount: String? = null,
)