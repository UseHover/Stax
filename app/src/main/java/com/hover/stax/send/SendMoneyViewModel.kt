package com.hover.stax.send

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class SendMoneyViewModel : ViewModel() {

    val uiState = MutableStateFlow(SendMoneyState())

    fun handleEvent(sendMoneyEvent: SendMoneyEvent) {
        when (sendMoneyEvent) {
            is SendMoneyEvent.AmountChanged -> TODO()
            SendMoneyEvent.Cancel -> TODO()
            SendMoneyEvent.ChangePaymentOption -> TODO()
            SendMoneyEvent.ChoosePaymentTypeOption -> TODO()
            SendMoneyEvent.Next -> TODO()
            is SendMoneyEvent.SelectPaymentOption -> TODO()
            is SendMoneyEvent.SelectPaymentTypeOption -> TODO()
            SendMoneyEvent.SendMoney -> TODO()
        }
    }
}