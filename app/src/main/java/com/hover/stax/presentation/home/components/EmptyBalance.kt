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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.views.theme.ColorSurface
import com.hover.stax.views.theme.DarkGray
import com.hover.stax.views.theme.OffWhite

@Composable
fun EmptyBalance(onClickedAddAccount: () -> Unit) {
    val size34 = dimensionResource(id = R.dimen.margin_34)
    val size16 = dimensionResource(id = R.dimen.margin_16)
    Column(modifier = Modifier.padding(vertical = size16)) {
        val modifier = Modifier.padding(horizontal = size34)

        Text(
            text = stringResource(id = R.string.your_accounts),
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(horizontal = size16)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.empty_balance_desc),
            style = MaterialTheme.typography.body1,
            color = colorResource(id = R.color.offWhite),
            textAlign = TextAlign.Center,
            modifier = modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onClickedAddAccount,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 0.dp)
                .then(modifier),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(width = 0.5.dp, color = DarkGray),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ColorSurface,
                contentColor = OffWhite
            )
        ) {
            Text(
                text = stringResource(id = R.string.add_account),
                style = MaterialTheme.typography.button,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, bottom = 5.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}