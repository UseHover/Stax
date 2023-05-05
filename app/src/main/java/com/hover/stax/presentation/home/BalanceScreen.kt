/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.hover.stax.database.models.Account
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