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
package com.hover.stax.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hover.stax.domain.model.Account
import com.hover.stax.presentation.add_accounts.components.SampleAccountProvider


@Composable
fun BalancesList(accounts: List<Account>?, onClickNewAccount: () -> Unit) {
    if (!accounts.isNullOrEmpty()) {
        Column() {
            BalanceHeader(showAddAccount = true, onClickNewAccount)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                accounts.forEach { account ->
                    BalanceItem(account, {}, {})
                    // balancesViewModel.requestBalance(account) }, { findNavController().navigate(HomeFragmentDirections.actionNavigationHomeToAccountDetailsFragment(accountId)) })
                }
            }
        }
    }
}

@Preview
@Composable
fun BalancesListPreview(@PreviewParameter(SampleAccountProvider::class) accounts: List<Account>) {
    BalancesList(accounts) {}
}