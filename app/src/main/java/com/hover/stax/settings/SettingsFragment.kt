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
package com.hover.stax.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.hover.sdk.api.Hover
import com.hover.stax.BuildConfig
import com.hover.stax.R
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.database.models.Account
import com.hover.stax.databinding.FragmentSettingsBinding
import com.hover.stax.languages.LanguageViewModel
import com.hover.stax.login.AbstractGoogleAuthActivity
import com.hover.stax.login.LoginScreenUiState
import com.hover.stax.login.LoginUiState
import com.hover.stax.login.LoginViewModel
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.core.Utils
import com.hover.stax.utils.collectLifecycleFlow
import com.hover.stax.views.StaxDialog
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

const val TEST_MODE = "test_mode"

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var accountAdapter: ArrayAdapter<Account>? = null
    private var clickCounter = 0

    private val accountsViewModel: AccountsViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()

    private var dialog: StaxDialog? = null

    private var optInMarketing: Boolean = false
    private var appInfoVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent(
            getString(
                R.string.visit_screen,
                getString(R.string.visit_security)
            ),
            requireActivity()
        )

        setUpShare()
        setUpMeta()
        setUpChooseLang()
        setUpManagePermissions()
        setupLearnCard()
        setUpSupport()
        setUpEnableTestMode()
        setupAppVersionInfo()
        setUpAccountDetails()

        binding.bountyCard.getStartedWithBountyButton.setOnClickListener { startBounties() }

        collectLifecycleFlow(accountsViewModel.defaultUpdateMsg) {
            UIHelper.flashAndReportMessage(requireActivity(), it)
        }
    }

    private fun setUpShare() {
        binding.shareCard.shareText.setOnClickListener { com.hover.stax.utils.Utils.shareStax(requireActivity()) }
    }

    private fun setUpManagePermissions() {
        binding.permissionCard.permission.setOnClickListener {
            NavUtil.navigate(
                findNavController(),
                SettingsFragmentDirections.actionNavigationSettingsToManagePermissionFragment()
            )
        }
    }

    private fun setUpMeta() {
        binding.settingsCard.connectAccounts.setOnClickListener {
            NavUtil.navigate(
                findNavController(),
                SettingsFragmentDirections.actionNavigationSettingsToNavigationLinkAccount()
            )
        }

        collectLifecycleFlow(accountsViewModel.accountList) {
            if (it.accounts.isEmpty()) {
                binding.settingsCard.defaultAccountEntry.visibility = GONE
                binding.settingsCard.connectAccounts.visibility = VISIBLE
            } else
                createDefaultSelector(it.accounts)
        }
    }

    private fun setUpChooseLang() {
        val selectLangBtn = binding.languageCard.selectLanguageBtn
        val languageVM: LanguageViewModel by activityViewModels()
        languageVM.languages.observe(viewLifecycleOwner) { langs ->
            langs.forEach {
                if (it.isSelected()) selectLangBtn.text = it.name
            }
        }

        selectLangBtn.setOnClickListener {
            NavUtil.navigate(
                findNavController(),
                SettingsFragmentDirections.actionNavigationSettingsToLanguageSelectFragment()
            )
        }
    }

    private fun getAppInfoVisibility(): Int {
        return if (appInfoVisible) GONE
        else VISIBLE
    }

    private fun setupAppVersionInfo() {
        binding.appInfoCard.appInfoDesc.setOnClickListener {
            with(binding.appInfoCard.details) {
                this.appInfo.visibility = getAppInfoVisibility()
                appInfoVisible = !appInfoVisible
            }
        }

        val deviceId = Hover.getDeviceId(requireContext())
        val appVersion: String = BuildConfig.VERSION_NAME
        val versionCode: String = BuildConfig.VERSION_CODE.toString()
        val configVersion: String? =
            Utils.getSdkPrefs(requireContext()).getString("channel_actions_schema_version", "")
        with(binding.appInfoCard.details) {
            this.appVersionInfo.text = getString(R.string.app_version_info, appVersion)
            this.appVersionInfo.setOnClickListener {
                com.hover.stax.utils.Utils.copyToClipboard(
                    appVersion,
                    requireContext()
                )
            }

            this.configVersionInfo.text = getString(R.string.config_info, configVersion)
            this.configVersionInfo.setOnClickListener {
                com.hover.stax.utils.Utils.copyToClipboard(
                    configVersion,
                    requireContext()
                )
            }

            this.versionCodeInfo.text = getString(R.string.version_code_info, versionCode)
            this.versionCodeInfo.setOnClickListener {
                com.hover.stax.utils.Utils.copyToClipboard(
                    versionCode,
                    requireContext()
                )
            }

            this.deviceIdInfo.text = getString(R.string.device_id_info, deviceId)
            this.deviceIdInfo.setOnClickListener {
                com.hover.stax.utils.Utils.copyToClipboard(
                    deviceId,
                    requireContext()
                )
            }
        }
    }

    private fun setUpAccountDetails() {
        loginViewModel.staxUser.observe(viewLifecycleOwner) { staxUser ->
            if (staxUser == null) {
                with(binding.accountCard) {
                    loggedInAccount.visibility = GONE
                    loginAccount.visibility = VISIBLE
                    loggedInHeader.text = getString(R.string.login_dialog_title)
                    loginAccount.setOnClickListener { startGoogleLogin() }
                }
            } else {
                binding.staxSupport.marketingOptIn.isChecked = staxUser.marketingOptedIn
                if (staxUser.isMapper) binding.bountyCard.root.visibility = VISIBLE

                if (optInMarketing && !staxUser.marketingOptedIn) {
                    marketingOptIn(true)
                    optInMarketing = false
                }

                with(binding.accountCard) {
                    loggedInAccount.visibility = VISIBLE
                    loginAccount.visibility = GONE
                    loggedInAccount.text = getString(R.string.logged_in_as, staxUser.username)
                    loggedInHeader.text = getString(R.string.logout)
                    accountLayout.setOnClickListener { showLogoutConfirmDialog() }
                }
            }
        }
    }

    private fun setUpSupport() {
        with(binding.staxSupport) {
            twitterContact.setOnClickListener {
                com.hover.stax.utils.Utils.openUrl(
                    getString(R.string.stax_twitter_url),
                    requireActivity()
                )
            }
            requestFeature.setOnClickListener {
                com.hover.stax.utils.Utils.openUrl(
                    getString(R.string.stax_nolt_url),
                    requireActivity()
                )
            }
            contactSupport.setOnClickListener {
                com.hover.stax.utils.Utils.openEmail(
                    getString(
                        R.string.stax_emailing_subject,
                        Hover.getDeviceId(requireActivity())
                    ),
                    requireActivity()
                )
            }
            faq.setOnClickListener {
                NavUtil.navigate(
                    findNavController(),
                    SettingsFragmentDirections.actionNavigationSettingsToFaqFragment()
                )
            }

            receiveStaxUpdate.setOnClickListener {
                if (loginViewModel.staxUser.value == null)
                    showLoginDialog()
                else
                    marketingOptIn(!marketingOptIn.isChecked)
            }
        }
    }

    private fun setupLearnCard() {
        with(binding.staxLearn) {
            learnFinances.setOnClickListener {
                NavUtil.navigate(
                    findNavController(),
                    SettingsFragmentDirections.actionNavigationSettingsToWellnessFragment()
                )
            }
            learnStax.setOnClickListener {
                com.hover.stax.utils.Utils.openUrl(
                    getString(R.string.stax_medium_url),
                    requireActivity()
                )
            }
        }
    }

    private fun createDefaultSelector(accounts: List<Account>) {
        binding.settingsCard.connectAccounts.visibility = GONE
        val spinner = binding.settingsCard.defaultAccountSpinner
        binding.settingsCard.defaultAccountEntry.visibility = VISIBLE
        accountAdapter = ArrayAdapter(requireActivity(), R.layout.stax_spinner_item, accounts)
        spinner.setAdapter(accountAdapter)

        val account = accounts.firstOrNull { it.isDefault }
        val defaultAccount = if (account != null)
            account
        else {
            val a = accounts.minByOrNull { it.id }
            a?.let { accountsViewModel.setDefaultAccount(it) }
            a
        }

        spinner.setText(defaultAccount?.userAlias, false)
        spinner.onItemClickListener = OnItemClickListener { _, _, pos: Int, _ ->
            if (pos != -1) {
                accountsViewModel.setDefaultAccount(accounts[pos])
            }
        }
    }

    private fun setUpEnableTestMode() {
        binding.settingsCard.testMode.setOnCheckedChangeListener { _, isChecked ->
            Utils.saveBoolean(TEST_MODE, isChecked, requireContext())
            UIHelper.flashAndReportMessage(
                requireContext(),
                if (isChecked) R.string.test_mode_toast else R.string.test_mode_disabled
            )
        }
        binding.settingsCard.testMode.visibility =
            if (Utils.getBoolean(TEST_MODE, requireContext())) VISIBLE else GONE
        binding.disclaimer.setOnClickListener {
            clickCounter++
            if (clickCounter == 5) UIHelper.flashAndReportMessage(
                requireContext(),
                R.string.test_mode_almost_toast
            ) else if (clickCounter == 7) enableTestMode()
        }
    }

    private fun enableTestMode() {
        Utils.saveBoolean(TEST_MODE, true, requireActivity())
        binding.settingsCard.testMode.visibility = VISIBLE
        UIHelper.flashAndReportMessage(requireContext(), R.string.test_mode_toast)
    }

    private fun startBounties() {
        val staxUser = loginViewModel.staxUser.value

        val navDirection = if (staxUser == null || !staxUser.isMapper)
            SettingsFragmentDirections.actionNavigationSettingsToBountyEmailFragment()
        else
            SettingsFragmentDirections.actionNavigationSettingsToBountyListFragment()
        NavUtil.navigate(findNavController(), navDirection)
    }

    private fun showLogoutConfirmDialog() {
        dialog = StaxDialog(requireActivity())
            .setDialogTitle(R.string.dialog_confirm_logout_header)
            .setDialogMessage(getString(R.string.dialog_confirm_logout_desc))
            .setNegButton(R.string.btn_cancel) { dialog?.dismiss() }
            .setPosButton(R.string.logout) { logoutUser() }
        dialog!!.showIt()
    }

    private fun logoutUser() {
        loginViewModel.silentSignOut()
        binding.staxSupport.marketingOptIn.isChecked = false
        UIHelper.flashAndReportMessage(requireActivity(), getString(R.string.logout_out_success))
    }

    private fun showLoginDialog() {
        dialog = StaxDialog(requireActivity())
            .setDialogTitle(R.string.login_dialog_title)
            .setDialogMessage(getString(R.string.login_dialog_desc))
            .setNegButton(R.string.btn_cancel) { dialog?.dismiss() }
            .setPosButton(R.string.btn_google_signin) {
                startGoogleLogin()
            }
        dialog!!.showIt()
    }

    private fun startGoogleLogin() {
        binding.staxSupport.contactCard.showProgressIndicator()
        optInMarketing = true
        (requireActivity() as AbstractGoogleAuthActivity).signIn()
    }

    private fun marketingOptIn(optedIn: Boolean) {
        binding.staxSupport.contactCard.showProgressIndicator()
        loginViewModel.optInMarketing(optedIn)

        updateLoginProgress(loginViewModel.loginState)
    }

    private fun updateLoginProgress(loginState: StateFlow<LoginScreenUiState>) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginState.collect {
                    when (it.loginState) {
                        LoginUiState.Loading -> {}
                        is Error -> {}
                        LoginUiState.Success -> {
                            binding.staxSupport.contactCard.hideProgressIndicator()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()

        _binding = null
    }
}