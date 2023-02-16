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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.hover.stax.R
import com.hover.stax.presentation.add_accounts.StaxTopBarDefaults
import com.hover.stax.ui.theme.OffWhite
import com.hover.stax.utils.network.NetworkMonitor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(@StringRes title: Int = 0, navTo: (dest: Int) -> Unit) {
    val hasNetwork by NetworkMonitor.StateLiveData.get().observeAsState(initial = true)

    CenterAlignedTopAppBar(
        title = {
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                if (title != 0) { Text(stringResource(id = title), style = MaterialTheme.typography.h4) }
                if (!hasNetwork) {
                    HorizontalImageTextView(
                        drawable = R.drawable.ic_internet_off,
                        stringRes = R.string.working_offline,
                        MaterialTheme.typography.button
                    )
                }
            }
        },
        actions = { IconButton(onClick = { navTo(R.id.action_global_NavigationSettings) }) {
            Icon(painterResource(id = R.drawable.ic_settings),
                stringResource(R.string.nav_settings), tint = OffWhite
            )
        } },
        colors = StaxTopBarDefaults()
    )
}