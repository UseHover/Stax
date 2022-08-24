package com.hover.stax.presentation.rewards.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.ui.theme.BrightBlue
import com.hover.stax.ui.theme.OffWhite
import com.hover.stax.ui.theme.StaxTheme

@Composable
fun RewardItem(points: Int, action: String) {
    Column(
        modifier = Modifier.padding(2.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.margin_5))
                .drawBehind {
                    drawCircle(
                        color = BrightBlue,
                        radius = this.size.minDimension
                    )
                },
            text = points.toString(),
            style = MaterialTheme.typography.h2,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_10)))

        Text(
            text = action,
            color = OffWhite,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .padding(top = dimensionResource(id = R.dimen.margin_16))
                .widthIn(min = 50.dp, max = 65.dp)
        )
    }
}

@Preview
@Composable
fun RewardItemPreview() {
    StaxTheme {
        Surface(color = MaterialTheme.colors.background) {
            RewardItem(points = 100, action = "Buy airtime")
        }
    }
}