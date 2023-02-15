package com.hover.stax.send

import com.hover.stax.domain.model.Account

sealed class SendMoneyEvent {

    object ChangePaymentOption : SendMoneyEvent()

    data class SelectPaymentOption(val account: Account) : SendMoneyEvent()

    object ChoosePaymentTypeOption : SendMoneyEvent()

    data class SelectPaymentTypeOption(val account: Account) : SendMoneyEvent()

    data class AmountChanged(val account: Account) : SendMoneyEvent()

    object Cancel : SendMoneyEvent()

    object Next : SendMoneyEvent()

    object SendMoney : SendMoneyEvent()
}