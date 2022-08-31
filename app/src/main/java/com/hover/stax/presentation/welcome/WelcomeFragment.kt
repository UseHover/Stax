package com.hover.stax.presentation.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.hover.stax.R
import com.hover.stax.login.LoginViewModel
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.domain.model.StaxUser
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class WelcomeFragment : Fragment() {

    private lateinit var title: String
    private lateinit var subtitle: String
    private lateinit var buttonText: String

    private var dialog: StaxDialog? = null

    private val args: WelcomeFragmentArgs by navArgs()
    private val loginViewModel: LoginViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            id = R.id.welcomeFragment

            setGreetings(args.salutation)

            setContent {
                WelcomeScreen(title, subtitle, buttonText, { onClickGetStarted() }, { onClickLogin() })
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_welcome)), requireActivity())

        observeLoginProgress()
    }

    private fun onClickGetStarted() {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_getstarted), requireActivity())
        (requireActivity() as OnBoardingActivity).checkPermissionsAndNavigate()
    }

    private fun onClickLogin() {
        if (loginViewModel.staxUser.value != null) {
            UIHelper.flashAndReportMessage(requireActivity(), getString(R.string.signed_in_message))
            onClickGetStarted()
        } else {
            AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_google_sign_in), requireActivity())
            (requireActivity() as OnBoardingActivity).signIn()
        }
    }

    private fun observeLoginProgress() = with(loginViewModel) {
        error.observe(viewLifecycleOwner) { it?.let { showError(it) } }
        staxUser.observe(viewLifecycleOwner) {
            if (it != null) {
                showWelcomeMessage(it)
                onClickGetStarted()
            }
        }
    }

    private fun setGreetings(greeting: Int) = when (greeting) {
        1 -> {
            title = getString(R.string.welcome_title_one)
            subtitle = getString(R.string.welcome_sub_one)
            buttonText = getString(R.string.explore_btn_text)
        }
        2 -> {
            title = getString(R.string.welcome_title_two)
            subtitle = getString(R.string.welcome_sub_two)
            buttonText = getString(R.string.explore_btn_text)
        }
        3 -> {
            title = getString(R.string.welcome_title_three)
            subtitle = getString(R.string.welcome_sub_two)
            buttonText = getString(R.string.explore_btn_text)
        }
        else -> {
            title = getString(R.string.welcome_title_one)
            subtitle = getString(R.string.welcome_sub_one)
            buttonText = getString(R.string.explore_btn_text)
        }
    }

    private fun showError(message: String) {
        dialog = StaxDialog(requireActivity())
            .setDialogMessage(message)
            .setPosButton(R.string.btn_ok, null)
            .makeSticky()

        dialog!!.showIt()
    }

    private fun showWelcomeMessage(user: StaxUser) {
        val message = if (user.transactionCount > 0)
            getString(R.string.welcome_back, user.username)
        else
            getString(R.string.welcome, user.username)

        UIHelper.flashAndReportMessage(requireActivity(), message)
    }

    override fun onDestroyView() {
        if (dialog != null) dialog!!.dismiss()

        super.onDestroyView()
    }

}



