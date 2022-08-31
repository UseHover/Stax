package com.hover.stax.presentation.welcome.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.ui.theme.NavGrey
import com.hover.stax.ui.theme.OffWhite
import com.hover.stax.ui.theme.StaxTheme

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = NavGrey,
            contentColor = OffWhite
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.margin_8)),
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = R.drawable.google_logo),
                contentDescription = "",
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
                text = stringResource(id = R.string.continue_with_google),
                style = MaterialTheme.typography.button
            )
        }
    }
}

@Preview
@Composable
fun GoogleSignInButtonPreview() {
    StaxTheme {
        Surface(color = MaterialTheme.colors.background) {
            GoogleSignInButton {}
        }
    }
}