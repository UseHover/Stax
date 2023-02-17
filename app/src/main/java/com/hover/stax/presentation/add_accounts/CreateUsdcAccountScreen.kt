package com.hover.stax.presentation.add_accounts

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.hover.stax.R
import com.hover.stax.addAccounts.UsdcViewModel
import com.hover.stax.presentation.add_accounts.components.InputPinScreen
import com.hover.stax.presentation.add_accounts.components.USDCExplainerScreen
import com.hover.stax.presentation.components.TallTopBar
import org.koin.androidx.compose.getViewModel

const val EXPLAIN = "explain"
const val PIN1 = "pin1"
const val PIN2 = "pin2"
const val SUMMARY = "summary"

@Composable
fun CreateUsdcAccountScreen(viewModel: UsdcViewModel = getViewModel(), navController: NavController) {

	val enterPin = remember { mutableStateOf("") }
	val confirmPin = remember { mutableStateOf("") }

	val currentPage = remember { mutableStateOf(EXPLAIN) }

	Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
		Crossfade(targetState = currentPage.value) { page ->
			when (page) {
				EXPLAIN -> USDCExplainerScreen(currentPage) { navController.popBackStack() }
				PIN1 -> PinEntryScreen(R.string.create_pin, enterPin, R.string.btn_continue,
					doneAction = {
						viewModel.setPin(enterPin.value)
						currentPage.value = PIN2
					},
					backAction = { navController.popBackStack() })
				PIN2 -> PinEntryScreen(R.string.confirm_pin, confirmPin, R.string.create_account,
					doneAction = { onConfirmPin(confirmPin.value, currentPage, viewModel) },
					backAction = {
						enterPin.value = ""
						confirmPin.value = ""
						currentPage.value = PIN1
					})
				SUMMARY -> UsdcAccountSummaryScreen(viewModel)
			}
		}
	}
}

private fun onConfirmPin(pin: String, currentPage: MutableState<String>, viewModel: UsdcViewModel) {
	if (viewModel.confirmPin(pin)) {
		viewModel.createAccount()
		currentPage.value = SUMMARY
	}
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PinEntryScreen(title: Int, pin: MutableState<String>, doneText: Int, doneAction: () -> Unit, backAction: () -> Unit) {
	Scaffold(
		topBar = { TallTopBar(stringResource(title), backAction) },
	) {
		InputPinScreen(pin, doneText, doneAction)
	}
}