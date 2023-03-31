package com.hover.stax.presentation.send_money

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.presentation.add_accounts.bottomSheetShape
import com.hover.stax.ui.theme.Background
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SendMoneyScreen(
    viewmodel: AccountsViewModel,
    navController: NavController
) {

    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        ModalBottomSheetLayout(
            sheetState = bottomSheetState,
            sheetShape = bottomSheetShape(),
            sheetBackgroundColor = Background,
            sheetContent = { }
        ) {
            Scaffold(
                topBar = { }
            ) { paddingValues ->
                SendMoneyOptionsScreen(
                    modifier = Modifier.padding(paddingValues),
                    navigateToTransactionScreen = {
                        navController.navigate("sendMoneyTransaction")
                    })
                BackHandler(bottomSheetState.isVisible) {
                    coroutineScope.launch { bottomSheetState.hide() }
                }
            }
        }
    }
}

@Composable
fun SendMoneyOptionsScreen(
    modifier: Modifier,
    navigateToTransactionScreen: () -> Unit
) {

}

@Composable
fun PayWithBottomSheet(
    modifier: Modifier,
    navigateToTransactionScreen: () -> Unit
) {

}

@Composable
fun PaymentTypeBottomSheet(
    modifier: Modifier,
    navigateToTransactionScreen: () -> Unit
) {

}