package com.hover.stax.presentation.bounties.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hover.stax.R
import com.hover.stax.domain.model.Bounty
import com.hover.stax.presentation.bounties.*
import com.hover.stax.ui.theme.Brutalista

@Composable
fun BountyCard(bounty: Bounty, bountyViewModel: BountyViewModel) {
    val context = LocalContext.current
    val margin8 = dimensionResource(id = R.dimen.margin_8)
    val margin13 = dimensionResource(id = R.dimen.margin_13)
    val margin5 = dimensionResource(id = R.dimen.margin_5)

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
            .clickable { bountyState.bountySelectEvent?.let { bountyViewModel.handleBountyEvent(it) } }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = margin13),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = bounty.generateDescription(context).replaceFirstChar { it.uppercase() },
                modifier = Modifier
                    .padding(top = margin8, bottom = margin8, end = margin13)
                    .weight(1f),
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
            SpannableImageTextView(
                drawable = bountyState.icon,
                stringRes = bountyState.msg,
                modifier = Modifier
                    .padding(start = margin13, end = margin13, top = margin5, bottom = margin5),
            )
    }
}

private fun getBountyState(bounty: Bounty): BountyItemState {
    return when {
        bounty.hasSuccessfulTransactions() ->
            BountyItemState(color = R.color.muted_green, msg = R.string.done, icon = R.drawable.ic_check, isOpen = false, bountySelectEvent = null)
        bounty.isLastTransactionFailed() && !bounty.action.bounty_is_open ->
            BountyItemState(color = R.color.stax_bounty_red_bg, msg = R.string.bounty_transaction_failed, icon = R.drawable.ic_error, isOpen = false, bountySelectEvent = BountySelectEvent.ViewTransactionDetail(bounty.transactions.last().uuid))
        bounty.isLastTransactionFailed() && bounty.action.bounty_is_open ->
            BountyItemState(color = R.color.stax_bounty_red_bg, msg = R.string.bounty_transaction_failed_try_again, icon = R.drawable.ic_error, isOpen = true, bountySelectEvent = BountySelectEvent.ViewBountyDetail(bounty))
        !bounty.action.bounty_is_open ->
            BountyItemState(color = R.color.lighter_grey, isOpen = false, bountySelectEvent = null)
        bounty.transactionCount > 0 ->
            BountyItemState(color = R.color.pending_brown, msg = R.string.bounty_pending_short_desc, icon = R.drawable.ic_warning, isOpen = true, bountySelectEvent = BountySelectEvent.ViewTransactionDetail(bounty.transactions.last().uuid))
        else ->
            BountyItemState(color = R.color.colorSurface, isOpen = true, bountySelectEvent = BountySelectEvent.ViewBountyDetail(bounty))
    }
}


@Preview
@Composable
fun BountyCardPreview() {
    val margin13 = dimensionResource(id = R.dimen.margin_13)
    val margin8 = dimensionResource(id = R.dimen.margin_8)

    Column(
        modifier = Modifier
            .background(color = colorResource(id = R.color.colorSurface))
            .padding(vertical = margin8)
    ) {
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

        SpannableImageTextView(
            drawable = R.drawable.ic_error,
            stringRes = R.string.bounty_transaction_failed,
            modifier = Modifier.padding(start = margin8, end = margin13, top = 5.dp, bottom = dimensionResource(id = R.dimen.margin_10)),
        )
    }
}