package com.hover.stax.presentation.add_account

import androidx.compose.runtime.Composable
import com.hover.stax.R
import com.hover.stax.channels.Channel

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(var icon: Int, var title: String, var screen: ComposableFun) {
	data class MobileMoney(val channels: List<Channel>?) : TabItem(R.drawable.ic_phone, "Mobile Money", { MobileMoneyScreen(channels) })
	data class Bank(val channels: List<Channel>?) : TabItem(R.drawable.ic_bank, "Bank", { BankScreen(channels) })
	object Crypto : TabItem(R.drawable.ic_crypto, "Crypto", { CryptoScreen() })
}
