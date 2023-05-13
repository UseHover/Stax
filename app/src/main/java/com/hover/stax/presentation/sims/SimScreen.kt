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
package com.hover.stax.presentation.sims

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.hover.stax.R
import com.hover.stax.database.models.Account
import com.hover.stax.domain.use_case.sims.SimWithAccount
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.presentation.home.components.TopBar
import com.hover.stax.presentation.sims.components.LinkSimCard
import com.hover.stax.presentation.sims.components.SampleSimInfoProvider
import com.hover.stax.presentation.sims.components.SimItem
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.ui.theme.TextGrey

@Composable
fun SimScreen(
    refreshBalance: (Account) -> Unit,
    buyAirtime: (Account) -> Unit,
    navTo: (dest: Int) -> Unit,
    simViewModel: SimViewModel = hiltViewModel()
) {
    val sims by simViewModel.sims.collectAsState()

    val context = LocalContext.current

    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Scaffold(
                topBar = {
                    TopBar(title = R.string.your_sim_cards, navTo)
                }
            ) { innerPadding ->
                val paddingModifier = Modifier.padding(innerPadding)

                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(id = R.dimen.margin_13))
                        .then(paddingModifier)
                ) {
                    if (sims.isEmpty()) {
                        if (PermissionUtils.hasPhonePermission(context)) {
                            item {
                                NoticeText(stringRes = R.string.loading)
                            }
                        } else {
                            item {
                                ShowGrantPermissionContent()
                            }
                        }
                    } else {
                        val comparator = Comparator { s1: SimWithAccount, s2: SimWithAccount ->
                            return@Comparator if (s1.sim.slotIdx == -1) { 1 } else if (s2.sim.slotIdx == -1) { -1 } else { s1.sim.slotIdx - s2.sim.slotIdx }
                        }

                        items(sims.sortedWith(comparator)) { sim ->
                            // Don't show removed SIM cards that we don't support, it is confusing
                            if (sim.account.channelId != -1 || sim.sim.slotIdx != -1)
                                SimItem(sim, refreshBalance, buyAirtime)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SimScreenPreview(
    @PreviewParameter(SampleSimInfoProvider::class) sims: List<SimWithAccount>
) {
    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Scaffold(topBar = {
                TopBar(title = R.string.your_sim_cards, {})
            }, content = { innerPadding ->
                val paddingModifier = Modifier.padding(innerPadding)

                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(id = R.dimen.margin_13))
                        .then(paddingModifier)
                ) {

                    if (sims.isEmpty())
                        item {
                            NoticeText(stringRes = R.string.simpage_permission_alert)
                        }

                    sims.let {
                        if (it.isEmpty()) {
                            item {
                                LinkSimCard(id = R.string.link_sim_to_stax)
                            }
                        }
                    }

                    itemsIndexed(sims) { index, sim ->
                        SimItem(
                            simWithAccount = sim,
                            { }, { }
                        )
                    }
                }
            })
        }
    }
}

@Composable
private fun NoticeText(@StringRes stringRes: Int) {
    val size13 = dimensionResource(id = R.dimen.margin_13)
    val size34 = dimensionResource(id = R.dimen.margin_34)

    Text(
        text = stringResource(id = stringRes),
        modifier = Modifier
            .padding(vertical = size13, horizontal = size34)
            .fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = TextGrey,
    )
}

@Composable
private fun ShowGrantPermissionContent() {
    Column {
        NoticeText(stringRes = R.string.simpage_permission_alert)
        LinkSimCard(id = R.string.link_sim_to_stax)
    }
}