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
    onBuyDataClicked: () -> Unit,
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
        VerticalImageTextView(
            onItemClick = onBuyDataClicked,
            drawable = R.drawable.ic_data,
            stringRes = R.string.cta_buy_data
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