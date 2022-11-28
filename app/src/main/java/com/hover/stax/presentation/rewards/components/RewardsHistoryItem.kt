package com.hover.stax.presentation.rewards.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.hover.stax.R
import com.hover.stax.ui.theme.StaxTheme

@Composable
fun RewardsHistoryItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.margin_16),
                vertical = dimensionResource(id = R.dimen.margin_8)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = dimensionResource(id = R.dimen.margin_8)),
                text = "Linked your first account",
                style = MaterialTheme.typography.body1
            )

            Text(
                modifier = Modifier.wrapContentWidth(),
                text = "100 points",
                style = MaterialTheme.typography.body1
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_5)))

        Text(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.End),
            text = "20 mins ago",
            style = MaterialTheme.typography.subtitle2
        )
    }
}

@Preview
@Composable
fun RewardsHistoryItemPreview() {
    StaxTheme {
        Surface(modifier = Modifier.wrapContentSize(), color = MaterialTheme.colors.background) {
            RewardsHistoryItem()
        }
    }
}