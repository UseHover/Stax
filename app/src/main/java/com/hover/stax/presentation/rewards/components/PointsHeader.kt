package com.hover.stax.presentation.rewards.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hover.stax.R
import com.hover.stax.ui.theme.*

@Composable
fun PointsHeader(points: Int, onClickRedeem: () -> Unit) {
    Row(
        modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_16))
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = points.toString(),
                style = MaterialTheme.typography.h2,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(id = R.string.stax_points),
                style = MaterialTheme.typography.h3
            )
        }

        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_10)))

        Button(
            onClick = onClickRedeem,
            modifier = Modifier
                .shadow(elevation = 0.dp)
                .wrapContentSize(Alignment.Center),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = BrightBlue,
                contentColor = TextColorDark
            )
        ) {
            Text(
                text = stringResource(id = R.string.redeem),
                style = MaterialTheme.typography.button,
                textAlign = TextAlign.Center,
                fontSize = 15.sp
            )
        }
    }
}

@Preview
@Composable
fun PointsHeaderPreview() {
    StaxTheme {
        Surface(modifier = Modifier.wrapContentSize(), color = colors.background) {
            PointsHeader(points = 100, onClickRedeem = {})
        }
    }
}