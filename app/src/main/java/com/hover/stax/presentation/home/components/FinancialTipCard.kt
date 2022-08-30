package com.hover.stax.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.presentation.home.FinancialTipClickInterface
import com.hover.stax.presentation.home.HomeViewModel

@Composable
fun FinancialTipCard(
    tipInterface: FinancialTipClickInterface?,
    financialTip: FinancialTip,
    homeViewModel: HomeViewModel?
) {
    val size13 = dimensionResource(id = R.dimen.margin_13)

    Card(elevation = 0.dp, modifier = Modifier.padding(all = size13)) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = size13)
            ) {
                HorizontalImageTextView(
                    drawable = R.drawable.ic_tip_of_day,
                    stringRes = R.string.tip_of_the_day,
                    Modifier.weight(1f),
                    MaterialTheme.typography.button
                )

                Image(painter = painterResource(id = R.drawable.ic_close_white),
                    contentDescription = null,
                    alignment = Alignment.CenterEnd,
                    modifier = Modifier.clickable { homeViewModel?.dismissTip(financialTip.id) })
            }

            Row(modifier = Modifier
                .padding(horizontal = size13)
                .clickable { tipInterface?.onTipClicked(null) }) {

                Column(modifier = Modifier.weight(1f)) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = financialTip.title,
                        style = MaterialTheme.typography.body2,
                        textDecoration = TextDecoration.Underline
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = financialTip.snippet,
                        style = MaterialTheme.typography.body2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = size13, top = 3.dp)
                    )

                    Text(text = stringResource(id = R.string.read_more),
                        color = colorResource(id = R.color.brightBlue),
                        modifier = Modifier
                            .padding(bottom = size13)
                            .clickable { tipInterface?.onTipClicked(financialTip.id) }
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.tips_fancy_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .padding(start = size13)
                        .align(Alignment.CenterVertically),
                )
            }
        }
    }
}