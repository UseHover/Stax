package com.hover.stax.onboarding.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.ui.theme.BrightBlue
import com.hover.stax.ui.theme.ColorPrimaryDark
import com.hover.stax.ui.theme.StaxTheme

@Composable
fun WelcomeHeader(title: String, desc: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.h1
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun FeatureCard(title: String, desc: String, iconRes: Int) {
    Row(modifier = Modifier.padding(top = 10.dp)) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.h3
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Composable
fun ContinueButton(text: String, modifier: Modifier = Modifier, onClick: (() -> Unit)) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = BrightBlue,
            contentColor = ColorPrimaryDark
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.button
        )
    }
}

@Composable
fun WelcomeScreen(introTitle: String, introDesc: String, buttonText: String, onClick: (() -> Unit)) {

    StaxTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Scaffold(
                modifier = Modifier.padding(21.dp),
                content = { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        WelcomeHeader(
                            title = introTitle,
                            desc = introDesc
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Column {
                            FeatureCard(
                                title = stringResource(R.string.intro_feature_one_title),
                                desc = stringResource(R.string.intro_feature_one_desc),
                                iconRes = R.drawable.ic_automated
                            )

                            FeatureCard(
                                title = stringResource(R.string.intro_feature_two_title),
                                desc = stringResource(R.string.intro_feature_two_desc),
                                iconRes = R.drawable.ic_control
                            )

                            FeatureCard(
                                title = stringResource(R.string.intro_feature_three_title),
                                desc = stringResource(R.string.intro_feature_three_desc),
                                iconRes = R.drawable.ic_safe
                            )
                        }
                    }
                },
                bottomBar = {
                    ContinueButton(text = buttonText, onClick = onClick)
                }
            )
        }
    }
}