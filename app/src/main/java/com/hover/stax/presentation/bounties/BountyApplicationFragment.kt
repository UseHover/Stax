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
package com.hover.stax.presentation.bounties

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.FragmentBountyApplicationBinding
import com.hover.stax.database.models.StaxUser
import com.hover.stax.home.MainActivity
import com.hover.stax.login.LoginScreenUiState
import com.hover.stax.login.LoginUiState
import com.hover.stax.login.LoginViewModel
import com.hover.stax.core.AnalyticsUtil.logAnalyticsEvent
import com.hover.stax.utils.NavUtil
import com.hover.stax.core.network.NetworkMonitor
import com.hover.stax.views.StaxDialog
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BountyApplicationFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentBountyApplicationBinding? = null
    private val binding get() = _binding!!
    private var dialog: StaxDialog? = null
    private lateinit var networkMonitor: NetworkMonitor
    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBountyApplicationBinding.inflate(inflater, container, false)
        networkMonitor = NetworkMonitor(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressIndicator.setVisibilityAfterHide(View.GONE)
        binding.instructions.apply {
            text = HtmlCompat.fromHtml(getString(R.string.bounty_email_stage_desc2), HtmlCompat.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
        }

        startObservers()
    }

    private fun startObservers() {
        with(loginViewModel) {
            updateLoginProgress(loginState)
            error.observe(viewLifecycleOwner) { it?.let { showError(it) } }
            staxUser.observe(viewLifecycleOwner) { initUI(it) }
        }
    }

    private fun initUI(staxUser: StaxUser?) = with(binding) {
        when {
            staxUser != null -> {
                btnSignIn.visibility = View.GONE
                signedInDetails.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.signed_in_as, staxUser.email)
                }
            }
            staxUser != null && staxUser.isMapper -> NavUtil.navigate(findNavController(), BountyApplicationFragmentDirections.actionBountyApplicationFragmentToBountyListFragment())
            else -> {
                signedInDetails.visibility = View.GONE
                btnSignIn.apply {
                    visibility = View.VISIBLE
                    setOnClickListener(this@BountyApplicationFragment)
                }
            }
        }
    }

    override fun onClick(v: View) {
        if (networkMonitor.isNetworkConnected) {
            startGoogleSignIn()
        } else {
            showDialog(R.string.internet_required, getString(R.string.internet_required_bounty_desc), R.string.btn_ok)
        }
    }

    private fun startGoogleSignIn() {
        logAnalyticsEvent(getString(R.string.clicked_bounty_email_continue_btn), requireContext())
        updateProgress(0)
        (activity as MainActivity).signIn()
    }

    // TODO - delete me
    private fun updateProgress(progress: Int) = with(binding.progressIndicator) {
        when (progress) {
            0 -> show()
            -1 -> hide()
            100 -> {
                hide()
                complete()
            }
            else -> setProgressCompat(progress, true)
        }
    }

    private fun updateLoginProgress(loginState: StateFlow<LoginScreenUiState>) = with(binding.progressIndicator) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginState.collect {
                    when (it.loginState) {
                        LoginUiState.Loading -> show()
                        LoginUiState.Error -> hide()
                        LoginUiState.Success -> {
                            hide()
                            complete()
                        }
                    }
                }
            }
        }
    }

    private fun complete() = NavUtil.navigate(findNavController(), BountyApplicationFragmentDirections.actionBountyApplicationFragmentToBountyListFragment())

    private fun showError(message: String) {
        updateProgress(-1)
        showDialog(0, message, R.string.btn_ok)
    }

    private fun showDialog(title: Int, msg: String, btn: Int) {
        dialog = StaxDialog(requireActivity())
            .setDialogMessage(msg)
            .setPosButton(btn, null)
            .makeSticky()

        if (title != 0)
            dialog?.setDialogTitle(title)

        dialog!!.showIt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
        _binding = null
    }
}