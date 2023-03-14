package com.hover.stax.presentation.add_accounts.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.presentation.add_accounts.EXPLAIN
import com.hover.stax.presentation.add_accounts.PIN1
import com.hover.stax.presentation.components.PrimaryButton
import com.hover.stax.presentation.components.TallTopBar

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun USDCExplainerScreen(currentPage: MutableState<String>, onBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        Scaffold(
            topBar = { TallTopBar(stringResource(R.string.create_usdc_explain_title)) { onBack() } },
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(34.dp), Arrangement.SpaceAround) {
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.usdc_bullet_1),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
                        style = MaterialTheme.typography.subtitle1
                    )
                    Text(
                        text = stringResource(id = R.string.usdc_explain_1),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
                        style = MaterialTheme.typography.body1
                    )
                }
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.usdc_bullet_2),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
                        style = MaterialTheme.typography.subtitle1
                    )
                    Text(
                        text = stringResource(id = R.string.usdc_explain_2),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
                        style = MaterialTheme.typography.body1
                    )
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp), Arrangement.Center) {
                    PrimaryButton(text = stringResource(R.string.continue_to_create_pin)) {
                        currentPage.value = PIN1
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun USDCExplainerScreenPreview() {
    val currentPage = remember { mutableStateOf(EXPLAIN) }
    USDCExplainerScreen(currentPage) {}
}