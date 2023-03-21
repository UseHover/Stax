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
	val errorMessage = remember { mutableStateOf(0) }

	Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
		Crossfade(targetState = currentPage.value) { page ->
			when (page) {
				EXPLAIN -> USDCExplainerScreen(currentPage) { navController.popBackStack() }
				PIN1 -> PinEntryScreen(R.string.create_pin, enterPin, R.string.btn_continue, errorMessage,
					doneAction = {
						if (enterPin.value.length > 3) {
							viewModel.setPin(enterPin.value)
							errorMessage.value = 0
							currentPage.value = PIN2
						} else {
							errorMessage.value = R.string.usdc_pin_length_error
							enterPin.value = ""
						}
					},
					backAction = { navController.popBackStack() })
				PIN2 -> PinEntryScreen(R.string.confirm_pin, confirmPin, R.string.create_account, errorMessage,
					doneAction = {
						if (viewModel.confirmPin(confirmPin.value)) {
							errorMessage.value = 0
							viewModel.createAccount()
							currentPage.value = SUMMARY
						} else {
							errorMessage.value = R.string.usdc_pin_match_error
							confirmPin.value = ""
						}
					},
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

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PinEntryScreen(title: Int, pin: MutableState<String>, doneText: Int, errorMessage: MutableState<Int>, doneAction: () -> Unit, backAction: () -> Unit) {
	Scaffold(
		topBar = { TallTopBar(stringResource(title), backAction) },
	) {
		InputPinScreen(pin, doneText, errorMessage, doneAction)
	}
}