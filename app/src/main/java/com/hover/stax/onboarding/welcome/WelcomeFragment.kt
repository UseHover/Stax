package com.hover.stax.onboarding.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.hover.stax.R
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.ui.theme.StaxTheme

class WelcomeFragment : Fragment() {

    private lateinit var title: String
    private lateinit var subtitle: String
    private lateinit var buttonText: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = ComposeView(requireContext()).apply {
        id = R.id.welcomeFragment

        setGreetings(arguments?.getInt(SALUTATIONS) ?: 1)

        setContent {
            WelcomeScreen(title, subtitle, buttonText) {
                (requireActivity() as OnBoardingActivity).checkPermissionsAndNavigate()
            }
        }
    }

    private fun setGreetings(greeting: Int) = when (greeting) {
        1 -> {
            title = getString(R.string.welcome_title_one)
            subtitle = getString(R.string.welcome_sub_one)
            buttonText = getString(R.string.btn_continue)
        }
        2 -> {
            title = getString(R.string.welcome_title_two)
            subtitle = getString(R.string.welcome_sub_two)
            buttonText = getString(R.string.btn_continue)
        }
        3 -> {
            title = getString(R.string.welcome_title_three)
            subtitle = getString(R.string.welcome_sub_two)
            buttonText = getString(R.string.explore_btn_text)
        }
        else -> {
            title = getString(R.string.welcome_title_one)
            subtitle = getString(R.string.welcome_sub_one)
            buttonText = getString(R.string.btn_continue)
        }
    }

    companion object {
        const val SALUTATIONS = "greetings"
    }
}


//This is only useful when you're building and want to see what the design looks like. It can be removed in the final version
@Preview
@Composable
fun WelcomeScreenPreview() {
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

                        Spacer(modifier = Modifier.height(16.dp))

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
                    ContinueButton(text = stringResource(id = R.string.explore_btn_text), onClick = {  })
                }
            )
        }
    }
}



