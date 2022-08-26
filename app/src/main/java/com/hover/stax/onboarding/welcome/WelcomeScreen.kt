package com.hover.stax.onboarding.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = desc,
            style = MaterialTheme.typography.body1
        )
    }
}

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
            modifier = modifier.padding(top = 5.dp, bottom = 5.dp),
            text = text,
            style = MaterialTheme.typography.button
        )
    }
}

@Composable
fun WelcomeScreen(introTitle: String, introDesc: String, buttonText: String, onClick: (() -> Unit)) {
    val features = getFeatures()

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
                            title = stringResource(R.string.welcome_title_one),
                            desc = stringResource(R.string.welcome_sub_one)
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_16)))

                        features.forEach {
                            FeatureCard(it)
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

@Preview
@Composable
fun WelcomeScreenPreview() {
    val features = getFeatures()

    StaxTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Scaffold(
                modifier = Modifier.padding(24.dp),
                content = { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {

                        WelcomeHeader(
                            title = stringResource(R.string.welcome_title_one),
                            desc = stringResource(R.string.welcome_sub_one)
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_16)))

                        features.forEach {
                            FeatureCard(it)
                        }
                    }
                },
                bottomBar = {
                    ContinueButton(text = stringResource(id = R.string.explore_btn_text), onClick = { })
                }
            )
        }
    }
}

data class Feature(val title: String, val desc: String, val iconRes: Int)

@Composable
fun getFeatures(): List<Feature> = listOf(
    Feature(
        title = stringResource(R.string.intro_feature_one_title),
        desc = stringResource(R.string.intro_feature_one_desc),
        iconRes = R.drawable.ic_automated
    ),
    Feature(
        title = stringResource(R.string.intro_feature_two_title),
        desc = stringResource(R.string.intro_feature_two_desc),
        iconRes = R.drawable.ic_control
    ),
    Feature(
        title = stringResource(R.string.intro_feature_three_title),
        desc = stringResource(R.string.intro_feature_three_desc),
        iconRes = R.drawable.ic_safe
    )
)




