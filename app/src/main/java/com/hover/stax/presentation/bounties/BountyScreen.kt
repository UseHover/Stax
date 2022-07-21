package com.hover.stax.presentation.bounties

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.presentation.home.HorizontalImageTextView
import com.hover.stax.ui.theme.StaxTheme

@Composable
fun ChannelBountiesCard(/*title: String, bounties: List<Bounty>*/) {
    Column {
        Text(
            text = "ACS Microfinance - *614*435# - NG".uppercase(),/*title*/
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.margin_13))
                .align(Alignment.End),
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold,
        )

        LazyColumn {
//            items(items = bounties, key = { it.action.public_id }) { bounty ->
//                BountyCard()
//            }
            items(10) {
                BountyCard()
            }
        }
    }
}

@Composable
fun BountyCard(/*bounty: Bounty*/modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val margin8 = dimensionResource(id = R.dimen.margin_8)
    val margin13 = dimensionResource(id = R.dimen.margin_13)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = margin13, end = margin13),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Check balance",/*bounty.generateDescription(context),*/
                modifier = Modifier
                    .padding(top = margin8, bottom = margin8, end = margin13),
                style = MaterialTheme.typography.body1
            )

            Text(
                text = "USD $1" /*stringResource(R.string.bounty_amount_with_currency, bounty.action.bounty_amount)*/,
                modifier = Modifier
                    .padding(top = margin8, bottom = margin8),
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Medium
            )
        }

        HorizontalImageTextView(
            drawable = R.drawable.ic_warning,
            stringRes = R.string.bounty_pending_short_desc,
            modifier = Modifier.padding(vertical = 5.dp, horizontal = margin13),
            MaterialTheme.typography.caption
        )
    }

}

@Preview
@Composable
fun BountiesPreview() {
    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            ChannelBountiesCard()
        }
    }
}