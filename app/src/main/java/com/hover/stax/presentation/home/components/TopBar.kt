package com.hover.stax.presentation.home.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hover.stax.R

@Composable
fun TopBar(@StringRes title: Int = R.string.app_name, isInternetConnected: Boolean, onClickedSettingsIcon: () -> Unit, onClickedRewards: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = dimensionResource(id = R.dimen.margin_13)),
    ) {
        HorizontalImageTextView(
            drawable = R.drawable.stax_logo,
            stringRes = title,
            modifier = Modifier.weight(1f),
            MaterialTheme.typography.button
        )

        if (!isInternetConnected) {
            HorizontalImageTextView(
                drawable = R.drawable.ic_internet_off,
                stringRes = R.string.working_offline,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 16.dp),
                MaterialTheme.typography.button
            )
        }

        /* TODO: Uncomment onces we are ready for rewards feature.
        Image(
            painter = painterResource(id = R.drawable.ic_rewards),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .clickable(onClick = onClickedRewards)
                .size(25.dp),
        ) */
        
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_10)))

        Image(
            painter = painterResource(id = R.drawable.ic_settings),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .clickable(onClick = onClickedSettingsIcon)
                .size(30.dp),
        )
    }
}