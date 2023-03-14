package com.hover.stax.presentation.add_accounts.components

import androidx.compose.runtime.Composable
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.presentation.add_accounts.ChannelList
import com.hover.stax.presentation.add_accounts.CryptoScreen

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(var icon: Int, var title: String, var screen: ComposableFun) {
    data class MobileMoney(val channels: List<Channel>?, val navigateToAdd: (Channel) -> Unit) : TabItem(R.drawable.ic_phone, "Mobile Money", { ChannelList(channels, "mmo", navigateToAdd) })
    data class Bank(val channels: List<Channel>?, val navigateToAdd: (Channel) -> Unit) : TabItem(R.drawable.ic_bank, "Bank", { ChannelList(channels, "bank", navigateToAdd) })
    data class Crypto(val navigateToUSDC: () -> Unit) : TabItem(R.drawable.ic_crypto, "Crypto", { CryptoScreen(navigateToUSDC) })
}