package com.hover.stax.views.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.hover.stax.R

@Composable
fun SendMoneyOption(
    modifier: Modifier = Modifier,
    selectedAccount: () -> Unit,
) {

    Row(
        horizontalArrangement = Arrangement.Start, modifier = Modifier
            .fillMaxWidth()
            .padding(all = dimensionResource(id = R.dimen.margin_13))
            .clickable(onClick = { selectedAccount() })
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_arrow_back),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterVertically)
        )
        Text(
            text = "",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterVertically),
            color = colorResource(id = R.color.offWhite)
        )
        Text(
            text = stringResource(id = R.string.payment_type_screen_change),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterVertically),
            color = colorResource(id = R.color.offWhite)
        )
    }
}

@Preview
@Composable
fun SendMoneyOptionPreview() {
    SendMoneyOption(selectedAccount = {})
}