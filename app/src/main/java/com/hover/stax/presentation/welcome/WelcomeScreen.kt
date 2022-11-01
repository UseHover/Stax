package com.hover.stax.presentation.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.presentation.components.PrimaryButton
import com.hover.stax.presentation.welcome.components.ContinueButton
import com.hover.stax.presentation.welcome.components.FeatureCard
import com.hover.stax.presentation.welcome.components.GoogleSignInButton
import com.hover.stax.presentation.welcome.components.WelcomeHeader
import com.hover.stax.ui.theme.StaxTheme


@Composable
fun WelcomeScreen(onClickContinue: () -> Unit, onClickSignIn: () -> Unit, showExploreButton : Boolean) {
    val features = getFeatures()

    StaxTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Scaffold(
                modifier = Modifier.padding(21.dp),
                content = { innerPadding ->
                    Column(modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState(), true)
                    ) {

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
                    Column {
                        GoogleSignInButton { onClickSignIn() }

                        if(showExploreButton) {
                            Spacer(modifier = Modifier.height(10.dp))
                            ContinueButton(text = stringResource(id = R.string.explore_btn_text), onClick = onClickContinue)
                        }
                    }
                }
            )
        }
    }
}

@Preview(device = Devices.NEXUS_5)
@Preview(device = Devices.NEXUS_6)
@Preview(device = Devices.DEFAULT)
@Composable
fun WelcomeScreenPreview() {
    val features = getFeatures()
    val showExploreButton = true

    StaxTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Scaffold(
                modifier = Modifier.padding(24.dp),
                content = { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState(), true)
                    ) {
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
                    Column {
                        GoogleSignInButton {}

                        if(showExploreButton) {
                            Spacer(modifier = Modifier.height(10.dp))
                            ContinueButton(text = stringResource(id = R.string.explore_btn_text), onClick = { })
                        }
                    }
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




