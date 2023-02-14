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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.hover.stax.presentation.bounties.BountySelectEvent
import com.hover.stax.presentation.bounties.BountyViewModel
import com.hover.stax.ui.theme.Brutalista

@Composable
fun BountyLi(bounty: Bounty, bountyViewModel: BountyViewModel) {
    val context = LocalContext.current
    val margin8 = dimensionResource(id = R.dimen.margin_8)
    val margin13 = dimensionResource(id = R.dimen.margin_13)
    val margin5 = dimensionResource(id = R.dimen.margin_5)

    val strikeThrough = TextStyle(
        fontFamily = Brutalista,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        textDecoration = TextDecoration.LineThrough
    )

    Column(
        modifier = Modifier
            .background(color = colorResource(id = getColor(bounty)))
            .padding(vertical = margin8)
            .clickable { bountyViewModel.handleBountyEvent(getTapAction(bounty)) }
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
                style = if (isOpen(bounty)) MaterialTheme.typography.body1 else strikeThrough
            )

            Text(
                text = stringResource(R.string.bounty_amount_with_currency, bounty.action.bounty_amount),
                modifier = Modifier
                    .padding(top = margin8, bottom = margin8),
                style = if (isOpen(bounty)) MaterialTheme.typography.body1 else strikeThrough,
                fontWeight = FontWeight.Medium
            )
        }

        if (getMsg(bounty) != 0)
            SpannableImageTextView(
                drawable = getIcon(bounty),
                stringRes = getMsg(bounty),
                modifier = Modifier
                    .padding(start = margin13, end = margin13, top = margin5, bottom = margin5),
            )
    }
}

private fun getColor(bounty: Bounty): Int {
    return when {
        bounty.hasSuccessfulTransactions() -> R.color.muted_green
        bounty.isLastTransactionFailed() -> R.color.stax_bounty_red_bg
        !bounty.action.bounty_is_open -> R.color.lighter_grey
        bounty.transactionCount > 0 -> R.color.pending_brown
        else -> R.color.colorBackground
    }
}

private fun getMsg(bounty: Bounty): Int {
    return when {
        bounty.hasSuccessfulTransactions() -> R.string.done
        bounty.isLastTransactionFailed() && !bounty.action.bounty_is_open -> R.string.bounty_transaction_failed
        bounty.isLastTransactionFailed() && bounty.action.bounty_is_open -> R.string.bounty_transaction_failed_try_again
        bounty.transactionCount > 0 -> R.string.bounty_pending_short_desc
        else -> 0
    }
}

private fun getIcon(bounty: Bounty): Int {
    return when {
        bounty.hasSuccessfulTransactions() -> R.drawable.ic_check
        bounty.isLastTransactionFailed() -> R.drawable.ic_error
        bounty.transactionCount > 0 -> R.drawable.ic_warning
        else -> 0
    }
}

private fun isOpen(bounty: Bounty): Boolean {
    return when {
        bounty.hasSuccessfulTransactions() -> false
        bounty.isLastTransactionFailed() && !bounty.action.bounty_is_open -> false
        bounty.isLastTransactionFailed() && bounty.action.bounty_is_open -> true
        !bounty.action.bounty_is_open -> false
        bounty.transactionCount > 0 -> true
        else -> true
    }
}

private fun getTapAction(bounty: Bounty): BountySelectEvent? {
    return when {
        bounty.hasSuccessfulTransactions() ->
            BountySelectEvent.ViewTransactionDetail(bounty.transactions.last().uuid)
        bounty.isLastTransactionFailed() && !bounty.action.bounty_is_open ->
            BountySelectEvent.ViewTransactionDetail(bounty.transactions.last().uuid)
        bounty.isLastTransactionFailed() && bounty.action.bounty_is_open ->
            BountySelectEvent.ViewBountyDetail(bounty)
        !bounty.action.bounty_is_open -> null
        bounty.transactionCount > 0 ->
            BountySelectEvent.ViewTransactionDetail(bounty.transactions.last().uuid)
        else -> BountySelectEvent.ViewBountyDetail(bounty)
    }
}

@Preview
@Composable
fun BountyCardPreview() {
    val margin13 = dimensionResource(id = R.dimen.margin_13)
    val margin8 = dimensionResource(id = R.dimen.margin_8)

    Column(
        modifier = Modifier
            .background(color = colorResource(id = R.color.colorBackground))
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