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
package com.hover.stax.presentation.bounties.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.hover.stax.R
import com.hover.stax.model.ChannelBounties
import com.hover.stax.presentation.bounties.BountyViewModel

@Composable
fun ChannelBountyCard(channelBounty: com.hover.stax.model.ChannelBounties, bountyViewModel: BountyViewModel) {
    if (channelBounty.bounties.isNotEmpty())
        Column {
            Text(
                text = channelBounty.channel.ussdName.uppercase(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.margin_13)),
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )

            channelBounty.bounties.forEach {
                BountyLi(bounty = it, bountyViewModel)
            }
        }
}

@Preview
@Composable
fun ChannelBountiesCardPreview() {
    Column {
        Text(
            text = "ACS Microfinance - *614*435# - NG".uppercase(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.margin_13)),
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )

        repeat(3) {
            BountyCardPreview()
        }
    }
}