package com.hover.stax.navigation

sealed class PaymentScreens(val name: String) {

    object SendMoney : PaymentScreens("send-money-screen") {
        val route = name
    }

    object PaymentTypeScreen : PaymentScreens("payment-type-screen") {
        val route = name
    }
}