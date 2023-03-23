package com.hover.stax.presentation.send_money

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
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
import com.hover.stax.presentation.add_accounts.bottomSheetShape
import com.hover.stax.ui.theme.Background
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SendMoneyScreen() {

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

                BackHandler(bottomSheetState.isVisible) {
                    coroutineScope.launch { bottomSheetState.hide() }
                }
            }
        }
    }
}