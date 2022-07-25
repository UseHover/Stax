package com.hover.stax.presentation.bounties

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hover.stax.R
import com.hover.stax.domain.model.Bounty
import com.hover.stax.domain.model.ChannelBounties
import com.hover.stax.presentation.home.HorizontalImageTextView
import com.hover.stax.ui.theme.Brutalista
import com.hover.stax.ui.theme.StaxTheme

@Composable
fun BountyList(bountyViewModel: BountiesViewModel) {
    val bountiesState by bountyViewModel.bountiesState.collectAsState()

    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            LazyColumn {
                items(bountiesState.bounties) { channelBounty ->
                    ChannelBountyCard(channelBounty = channelBounty)
                }
            }
        }
    }
}

@Composable
fun ChannelBountyCard(channelBounty: ChannelBounties) {
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
            BountyCard(bounty = it)
        }
    }
}

@Composable
fun BountyCard(bounty: Bounty) {
    val context = LocalContext.current
    val margin8 = dimensionResource(id = R.dimen.margin_8)
    val margin13 = dimensionResource(id = R.dimen.margin_13)

    val bountyState = getBountyState(bounty)

    val strikeThrough = TextStyle(
        fontFamily = Brutalista,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        textDecoration = TextDecoration.LineThrough
    )

    Column(
        modifier = Modifier
            .background(color = colorResource(id = bountyState.color))
            .padding(vertical = margin8)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = margin13),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = bounty.generateDescription(context),
                modifier = Modifier
                    .padding(top = margin8, bottom = margin8, end = margin13),
                style = if (bountyState.isOpen) MaterialTheme.typography.body1 else strikeThrough
            )

            Text(
                text = stringResource(R.string.bounty_amount_with_currency, bounty.action.bounty_amount),
                modifier = Modifier
                    .padding(top = margin8, bottom = margin8),
                style = if (bountyState.isOpen) MaterialTheme.typography.body1 else strikeThrough,
                fontWeight = FontWeight.Medium
            )
        }

        if (bountyState.msg != 0)
            HorizontalImageTextView(
                drawable = bountyState.icon,
                stringRes = bountyState.msg,
                modifier = Modifier
                    .padding(start = margin13, end = margin13, top = 5.dp, bottom = dimensionResource(id = R.dimen.margin_10)),
                MaterialTheme.typography.caption
            )
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

@Preview
@Composable
fun BountyCardPreview() {
    val margin13 = dimensionResource(id = R.dimen.margin_13)
    val margin8 = dimensionResource(id = R.dimen.margin_8)

    Column(modifier = Modifier.background(color = colorResource(id = R.color.colorSurface)).padding(vertical = margin8)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = margin13),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Check Balance",
                modifier = Modifier
                    .padding(top = margin8, bottom = margin8, end = margin13),
                style = MaterialTheme.typography.body1
            )

            Text(
                text = "USD $1",
                modifier = Modifier
                    .padding(top = margin8, bottom = margin8),
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Medium
            )
        }

        HorizontalImageTextView(
            drawable = R.drawable.ic_error,
            stringRes = R.string.bounty_transaction_failed,
            modifier = Modifier.padding(start = margin13, end = margin13, top = 5.dp, bottom = dimensionResource(id = R.dimen.margin_10)),
            MaterialTheme.typography.caption
        )
    }
}

@Preview
@Composable
fun BountiesPreview() {
    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            LazyColumn {
                items(5) {
                    ChannelBountiesCardPreview()
                }
            }
        }
    }
}

//TODO add click listeners
private fun getBountyState(bounty: Bounty): BountyItemState {
    return when {
        bounty.hasASuccessfulTransactions() -> BountyItemState(R.color.muted_green, R.string.done, R.drawable.ic_check, false) {}
        bounty.isLastTransactionFailed() && !bounty.action.bounty_is_open -> BountyItemState(R.color.stax_bounty_red_bg, R.string.bounty_transaction_failed, R.drawable.ic_error, false) {}
        bounty.isLastTransactionFailed() && bounty.action.bounty_is_open -> BountyItemState(R.color.stax_bounty_red_bg, R.string.bounty_transaction_failed_try_again, R.drawable.ic_error, true) {}
        !bounty.action.bounty_is_open -> BountyItemState(R.color.lighter_grey, 0, 0, false) {}
        bounty.transactionCount > 0 -> BountyItemState(R.color.pending_brown, R.string.bounty_pending_short_desc, R.drawable.ic_warning, true) {}
        else -> BountyItemState(R.color.colorSurface, 0, 0, true) {}
    }
}

data class BountyItemState(
    @ColorRes val color: Int,
    @StringRes val msg: Int,
    @DrawableRes val icon: Int,
    val isOpen: Boolean,
    val onItemClick: () -> Unit
)