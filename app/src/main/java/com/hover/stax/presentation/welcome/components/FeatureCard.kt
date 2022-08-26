package com.hover.stax.presentation.welcome.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.presentation.welcome.Feature

@Composable
fun FeatureCard(feature: Feature) {
    Row(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.margin_10))) {
        Image(
            painter = painterResource(id = feature.iconRes),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.h3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = feature.desc,
                style = MaterialTheme.typography.body1
            )
        }
    }
}