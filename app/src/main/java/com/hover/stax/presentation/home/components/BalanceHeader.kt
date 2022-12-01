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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hover.stax.R

@Composable
fun BalanceHeader(onClickedAddAccount: () -> Unit, accountExists: Boolean) {
    val size13 = dimensionResource(id = R.dimen.margin_13)

    Row(
        modifier = Modifier
            .padding(all = size13)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.your_accounts),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.h4
        )

        if (accountExists) {
            Text(
                text = stringResource(id = R.string.add_an_account),
                style = MaterialTheme.typography.body2,
                modifier = Modifier
                    .clickable(onClick = onClickedAddAccount)
                    .padding(end = 5.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_add_white_16),
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onClickedAddAccount)
                    .background(color = colorResource(id = R.color.brightBlue))
            )
        }
    }
}