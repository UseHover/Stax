package com.hover.stax.presentation.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hover.stax.domain.model.Account
import com.hover.stax.presentation.home.components.BalanceHeader
import com.hover.stax.presentation.home.components.BalanceItem
import com.hover.stax.presentation.home.components.EmptyBalance
import com.hover.stax.ui.theme.StaxTheme

interface BalanceTapListener {
    fun onTapBalanceRefresh(account: Account?)
    fun onTapBalanceDetail(accountId: Int)
}

@Preview
@Composable
fun BalanceScreenPreview() {
    StaxTheme {
        Surface {
            BalanceListForPreview(accountList = emptyList())
        }
    }
}

@Composable
private fun BalanceListForPreview(accountList: List<Account>) {

    if (accountList.isEmpty()) {
        EmptyBalance {}
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp)
        ) {
            item {
                BalanceHeader(onClickedAddAccount = {}, accountExists = false)
            }
            items(accountList) { account ->
                val context = LocalContext.current
                BalanceItem(staxAccount = account, context = context, balanceTapListener = null)
            }
        }
    }
}

