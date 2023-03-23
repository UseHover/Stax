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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hover.stax.R

@Composable
fun BalanceHeader(showAddAccount: Boolean, onClickedAddAccount: () -> Unit) {
    val c = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 13.dp).padding(horizontal = 13.dp), Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = R.string.your_accounts),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.h4
        )

        if (showAddAccount) {
            Box(modifier = Modifier.clickable(onClick = { onClickedAddAccount() })) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.add_an_account),
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_add_white_16),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(color = colorResource(id = R.color.brightBlue))
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun BalanceHeaderPreview() {
    BalanceHeader(onClickedAddAccount = { }, showAddAccount = true)
}

@Preview
@Composable
fun EmptyBalanceHeaderPreview() {
    BalanceHeader(onClickedAddAccount = { }, showAddAccount = false)
}