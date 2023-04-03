/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.presentation.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.database.user.entity.StaxUser
import com.hover.stax.login.LoginViewModel
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class WelcomeFragment : Fragment() {

    private var dialog: StaxDialog? = null
    private val loginViewModel: LoginViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(requireContext()).apply {
            id = R.id.welcomeFragment
            setContent {
                WelcomeScreen({ onClickGetStarted() }, { onClickLogin() }, showExploreButton = true)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_welcome)), requireActivity())

        observeLoginProgress()
    }

    private fun onClickGetStarted() {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_getstarted), requireActivity())
        NavUtil.navigate(findNavController(), WelcomeFragmentDirections.toInteractiveOnboardingFragment())
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