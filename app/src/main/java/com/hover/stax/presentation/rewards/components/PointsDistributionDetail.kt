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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.hover.stax.R
import com.hover.stax.ui.theme.StaxTheme

@Composable
fun PointsDistributionDetail(actionList: List<RewardActions>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.margin_16)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        actionList.forEach {
            ActionItem(points = it.points, action = it.action) {} // TODO add click listeners for each action
        }
    }
}

@Preview
@Composable
fun PointsDistributionDetailPreview() {
    val items = listOf(
        RewardActions(points = 100, action = "Buy airtime"),
        RewardActions(points = 10, action = "Refer a Friend"),
        RewardActions(points = 20, action = "Pay a bill"),
        RewardActions(points = 50, action = "Send money")
    )

    StaxTheme {
        Surface(modifier = Modifier.wrapContentSize(), color = MaterialTheme.colors.background) {
            PointsDistributionDetail(items)
        }
    }
}

data class RewardActions(val action: String, val points: Int)