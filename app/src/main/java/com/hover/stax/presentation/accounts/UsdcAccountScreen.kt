package com.hover.stax.presentation.accounts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hover.stax.accounts.AccountDetailViewModel
import com.hover.stax.presentation.components.DestructiveButton
import com.hover.stax.presentation.components.PrimaryButton
import com.hover.stax.presentation.home.BalancesViewModel
import com.hover.stax.presentation.home.components.BalanceItem
import com.hover.stax.presentation.transactions.TransactionHistoryList
import com.hover.stax.ui.theme.StaxTheme
import org.koin.androidx.compose.getViewModel

@Composable
fun UsdcAccountScreen(viewModel: AccountDetailViewModel = getViewModel(),
                      balancesViewModel: BalancesViewModel = getViewModel(),
                      navController: NavController = rememberNavController()) {

	val account by viewModel.account.collectAsState()

	StaxTheme {
		Surface(modifier = Modifier.fillMaxSize()) {
			account?.let {
				BalanceItem(account = it.account, goToDetail = {}, refresh = {  })

				TransactionHistoryList(it.transactions)

				Text("Account Details")
				Text("account number")
				it.account.accountNo?.let { it1 -> Text(it1) }

				Text("Nickname")

				PrimaryButton("Download secret key") {}

				DestructiveButton("Remove account") {}
			}
		}
	}
}