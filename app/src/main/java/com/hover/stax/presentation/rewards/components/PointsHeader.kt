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
package com.hover.stax.presentation.rewards.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hover.stax.R
import com.hover.stax.ui.theme.BrightBlue
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.ui.theme.TextColorDark

@Composable
fun PointsHeader(points: Int, onClickRedeem: () -> Unit) {
    Row(
        modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_16))
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = points.toString(),
                style = MaterialTheme.typography.h2,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(id = R.string.stax_points),
                style = MaterialTheme.typography.h3
            )
        }

        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_10)))

        Button(
            onClick = onClickRedeem,
            modifier = Modifier
                .shadow(elevation = 0.dp)
                .wrapContentSize(Alignment.Center),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = BrightBlue,
                contentColor = TextColorDark
            )
        ) {
            Text(
                text = stringResource(id = R.string.redeem),
                style = MaterialTheme.typography.button,
                textAlign = TextAlign.Center,
                fontSize = 15.sp
            )
        }
    }
}

@Preview
@Composable
fun PointsHeaderPreview() {
    StaxTheme {
        Surface(modifier = Modifier.wrapContentSize(), color = colors.background) {
            PointsHeader(points = 100, onClickRedeem = {})
        }
    }
}