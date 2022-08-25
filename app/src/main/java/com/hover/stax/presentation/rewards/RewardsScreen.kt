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
import com.hover.stax.presentation.rewards.components.*
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
                    TopBar(title = R.string.nav_earn, isInternetConnected = false, {}, {})
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
                        })
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