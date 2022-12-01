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

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.presentation.components.StaxCard

@Composable
fun BonusCard(message: String, onClickedTC: () -> Unit, onClickedTopUp: () -> Unit) {
    val size13 = dimensionResource(id = R.dimen.margin_13)
    val size10 = dimensionResource(id = R.dimen.margin_10)

    StaxCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = size13)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.get_rewarded),
                    style = MaterialTheme.typography.h3
                )
                Text(
                    text = message,
                    modifier = Modifier.padding(vertical = size10),
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = stringResource(id = R.string.tc_apply),
                    textDecoration = TextDecoration.Underline,
                    color = colorResource(id = R.color.brightBlue),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.clickable(onClick = onClickedTC)
                )
                Text(
                    text = stringResource(id = R.string.top_up),
                    color = colorResource(id = R.color.brightBlue),
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier
                        .padding(top = size13)
                        .clickable(onClick = onClickedTopUp)
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_bonus),
                contentDescription = stringResource(id = R.string.get_rewarded),
                modifier = Modifier
                    .size(70.dp)
                    .padding(start = size13)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    BonusCard(message = "Get some bonus when you do some thing", {}, {})
}