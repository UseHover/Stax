package com.hover.stax.presentation.welcome

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
import androidx.navigation.fragment.navArgs
import com.hover.stax.R
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.utils.AnalyticsUtil

class WelcomeFragment : Fragment() {

    private lateinit var title: String
    private lateinit var subtitle: String
    private lateinit var buttonText: String

    private val args: com.hover.stax.onboarding.welcome.WelcomeFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = ComposeView(requireContext()).apply {
        id = R.id.welcomeFragment

        setGreetings(args.salutation)

        setContent {
            WelcomeScreen(title, subtitle, buttonText) {
                AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_getstarted), requireActivity())
                (requireActivity() as OnBoardingActivity).checkPermissionsAndNavigate()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_welcome)), requireActivity())
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

}



