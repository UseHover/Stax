package com.hover.stax.presentation.add_account

import androidx.compose.runtime.Composable
import com.hover.stax.R

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(var icon: Int, var title: String, var screen: ComposableFun) {
	object MobileMoney : TabItem(R.drawable.ic_phone, "Mobile Money", { MobileMoneyScreen() })
	object Bank : TabItem(R.drawable.ic_bank, "Bank", { BankScreen() })
	object Crypto : TabItem(R.drawable.ic_crypto, "Crypto", { CryptoScreen() })
}
