package com.hover.stax.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hover.stax.R
import com.hover.stax.ui.theme.OffWhite
import com.hover.stax.ui.theme.StaxCardBlue
import com.hover.stax.ui.theme.StaxTheme

@Composable
fun GuideCard(message: String, buttonString: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .shadow(elevation = 0.dp)
            .background(shape = RoundedCornerShape(5.dp), color = StaxCardBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.margin_16))
        ) {
            Row {
                Text(
                    text = stringResource(id = R.string.beginners_guide_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.button,
                    color = OffWhite
                )

                Image(painter = painterResource(id = R.drawable.ic_close_white),
                    contentDescription = null,
                    alignment = Alignment.CenterEnd,
                    modifier = Modifier.clickable { })
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_10)))

            Text(
                text = message,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.h4,
                color = OffWhite
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_10)))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .shadow(elevation = 0.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = OffWhite,
                    contentColor = StaxCardBlue
                )
            ) {
                Text(
                    text = buttonString.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.button,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun GuideCardPreview() {
    StaxTheme {
        Surface(modifier = Modifier.wrapContentSize(), color = MaterialTheme.colors.background) {
            GuideCard(
                message = stringResource(id = R.string.beginners_guide_airtime),
                buttonString = stringResource(id = R.string.check_airtime_balance)
            ) {}
        }
    }
}

