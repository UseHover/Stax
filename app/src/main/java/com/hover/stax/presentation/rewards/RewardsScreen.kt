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
package com.hover.stax.presentation.rewards

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hover.stax.R
import com.hover.stax.presentation.home.components.TopBar
import com.hover.stax.presentation.rewards.components.PointsDistributionDetail
import com.hover.stax.presentation.rewards.components.PointsHeader
import com.hover.stax.presentation.rewards.components.RecentPointsHeader
import com.hover.stax.presentation.rewards.components.RewardActions
import com.hover.stax.presentation.rewards.components.RewardsHistoryItem
import com.hover.stax.ui.theme.StaxTheme

@Composable
fun RewardsScreen() {
    val items = listOf(
        RewardActions(points = 100, action = "Buy airtime"),
        RewardActions(points = 10, action = "Refer a Friend"),
        RewardActions(points = 20, action = "Pay a bill"),
        RewardActions(points = 50, action = "Send money")
    )

    StaxTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Scaffold(
                topBar = {
                    TopBar(title = R.string.nav_earn, {})
                },
                content = { padding ->
                    LazyColumn(
                        modifier = Modifier.padding(padding),
                        content = {
                            item {
                                PointsHeader(points = 1000) {}
                            }

                            item {
                                PointsDistributionDetail(actionList = items)
                            }

                            item {
                                RecentPointsHeader {}
                            }

                            repeat(9) {
                                item {
                                    RewardsHistoryItem()
                                }
                            }
                        }
                    )
                }
            )
        }
    }
}

@Preview
@Composable
fun RewardsScreenPreview() {
    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            RewardsScreen()
        }
    }
}