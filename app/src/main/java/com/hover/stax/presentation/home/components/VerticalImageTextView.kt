package com.hover.stax.presentation.home.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hover.stax.R

@Composable
fun VerticalImageTextView(
    @DrawableRes drawable: Int,
    @StringRes stringRes: Int,
    onItemClick: () -> Unit
) {
    val size24 = dimensionResource(id = R.dimen.margin_24)
    val blue = colorResource(id = R.color.stax_state_blue)

    Column(
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(48.dp)
                .background(blue),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = drawable),
                contentDescription = "",
                modifier = Modifier
                    .size(size24)
            )
        }

        Text(
            text = stringResource(id = stringRes),
            color = colorResource(id = R.color.offWhite),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .padding(top = dimensionResource(id = R.dimen.margin_16))
                .widthIn(min = 50.dp, max = 65.dp)
        )
    }
}