package com.hover.stax.presentation.send_money

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.hover.stax.ui.preview.DevicePreviews

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SendMoneySummaryScreen() {

    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        Scaffold(
            topBar = { },
        ) {

        }
    }
}

@DevicePreviews
@Composable
fun SendMoneySummaryScreenPreview() {

}