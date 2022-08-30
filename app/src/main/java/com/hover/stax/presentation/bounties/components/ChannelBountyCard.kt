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
import com.hover.stax.domain.model.ChannelBounties
import com.hover.stax.presentation.bounties.BountyViewModel

@Composable
fun ChannelBountyCard(channelBounty: ChannelBounties, bountyViewModel: BountyViewModel) {
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
                BountyCard(bounty = it, bountyViewModel)
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
