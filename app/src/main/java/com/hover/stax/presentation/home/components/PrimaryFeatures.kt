package com.hover.stax.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hover.stax.R

@Composable
fun PrimaryFeatures(
    onSendMoneyClicked: () -> Unit,
    onBuyAirtimeClicked: () -> Unit,
    onBuyGoodsClicked: () -> Unit,
    onPayBillClicked: () -> Unit,
    onRequestMoneyClicked: () -> Unit,
    showKenyaFeatures: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .padding(horizontal = 13.dp, vertical = 26.dp)
            .fillMaxWidth()
    ) {
        VerticalImageTextView(
            onItemClick = onSendMoneyClicked,
            drawable = R.drawable.ic_send_money,
            stringRes = R.string.cta_transfer
        )
        VerticalImageTextView(
            onItemClick = onBuyAirtimeClicked,
            drawable = R.drawable.ic_system_upate_24,
            stringRes = R.string.cta_airtime
        )
        if (showKenyaFeatures) {
            VerticalImageTextView(
                onItemClick = onBuyGoodsClicked,
                drawable = R.drawable.ic_shopping_cart,
                stringRes = R.string.cta_merchant
            )
            VerticalImageTextView(
                onItemClick = onPayBillClicked,
                drawable = R.drawable.ic_utility,
                stringRes = R.string.cta_paybill_linebreak
            )
        }
        VerticalImageTextView(
            onItemClick = onRequestMoneyClicked,
            drawable = R.drawable.ic_baseline_people_24,
            stringRes = R.string.cta_request
        )
    }
}