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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.utils.network.NetworkMonitor

@Composable
fun TopBar(@StringRes title: Int = R.string.app_name, navTo: (dest: Int) -> Unit) {
    val hasNetwork by NetworkMonitor.StateLiveData.get().observeAsState(initial = true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = dimensionResource(id = R.dimen.margin_13)),
    ) {
        HorizontalImageTextView(
            drawable = R.drawable.stax_logo,
            stringRes = title,
            modifier = Modifier.weight(1f),
            MaterialTheme.typography.button
        )

        if (!hasNetwork) {
            HorizontalImageTextView(
                drawable = R.drawable.ic_internet_off,
                stringRes = R.string.working_offline,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 16.dp),
                MaterialTheme.typography.button
            )
        }

        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_10)))

        Image(
            painter = painterResource(id = R.drawable.ic_settings),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .clickable(onClick = { navTo(R.id.action_global_NavigationSettings) })
                .size(30.dp),
        )
    }
}