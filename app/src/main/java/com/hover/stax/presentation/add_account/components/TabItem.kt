package com.hover.stax.presentation.add_account.components

import androidx.compose.runtime.Composable
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.presentation.add_account.ChannelList
import com.hover.stax.presentation.add_account.CryptoScreen

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(var icon: Int, var title: String, var screen: ComposableFun) {
	data class MobileMoney(val channels: List<Channel>?, val navigateToAdd: (Int) -> Unit) : TabItem(R.drawable.ic_phone, "Mobile Money", { ChannelList(channels, "mmo", navigateToAdd) })
	data class Bank(val channels: List<Channel>?, val navigateToAdd: (Int) -> Unit) : TabItem(R.drawable.ic_bank, "Bank", { ChannelList(channels, "bank", navigateToAdd) })
	data class Crypto(val navigateToUSDC: () -> Unit) : TabItem(R.drawable.ic_crypto, "Crypto", { CryptoScreen(navigateToUSDC) })
}
