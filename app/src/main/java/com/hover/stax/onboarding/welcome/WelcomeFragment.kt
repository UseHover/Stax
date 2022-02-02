package com.hover.stax.onboarding.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.hover.stax.R
import com.hover.stax.onboarding.OnBoardingActivity

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



