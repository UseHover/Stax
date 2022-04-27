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
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.hover.sdk.api.Hover
import com.hover.stax.BuildConfig
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.databinding.FragmentSettingsBinding
import com.hover.stax.languages.LanguageViewModel
import com.hover.stax.login.LoginViewModel
import com.hover.stax.utils.*
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var accountAdapter: ArrayAdapter<Account>? = null
    private var clickCounter = 0

    private val accountsViewModel: AccountsViewModel by sharedViewModel()
    private val loginViewModel: LoginViewModel by sharedViewModel()

    private var dialog: StaxDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_security)), requireActivity())

        setUpShare()
        setUpMeta()
        setUpChooseLang()
        setupLearnCard()
        setUpSupport()
        setUpEnableTestMode()
        setupAppVersionInfo()
        setUpAccountCard()

        binding.bountyCard.getStartedWithBountyButton.setOnClickListener { startBounties() }
    }

    private fun setUpShare() {
        binding.shareCard.shareText.setOnClickListener { Utils.shareStax(requireActivity()) }
        if (loginViewModel.usernameIsNotSet()) loginViewModel.uploadLastUser()
    }

    private fun setUpMeta() {
        binding.settingsCard.connectAccounts.setOnClickListener {
            NavUtil.navigate(findNavController(), SettingsFragmentDirections.actionNavigationSettingsToNavigationLinkAccount())
        }

        accountsViewModel.accounts.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                binding.settingsCard.defaultAccountEntry.visibility = GONE
                binding.settingsCard.connectAccounts.visibility = VISIBLE
            } else
                createDefaultSelector(it)
        }
    }

    private fun setUpChooseLang() {
        val selectLangBtn = binding.languageCard.selectLanguageBtn
        val languageVM = getViewModel<LanguageViewModel>()
        languageVM.languages.observe(viewLifecycleOwner) { langs ->
            langs.forEach {
                if (it.isSelected()) selectLangBtn.text = it.name
            }
        }

        selectLangBtn.setOnClickListener { NavUtil.navigate(findNavController(), SettingsFragmentDirections.actionNavigationSettingsToLanguageSelectFragment()) }
    }

    private fun setupAppVersionInfo() {
        val deviceId = Hover.getDeviceId(requireContext())
        val appVersion: String = BuildConfig.VERSION_NAME
        val versionCode: String = BuildConfig.VERSION_CODE.toString()
        binding.staxAndDeviceInfo.text = getString(R.string.app_version_and_device_id, appVersion, versionCode, deviceId)
    }

    private fun setUpAccountCard() {
        loginViewModel.username.observe(viewLifecycleOwner) { username ->
            with(binding.accountCard) {
                if (!username.isNullOrEmpty()) {
                    accountCard.visibility = VISIBLE
                    loggedInAccount.text = getString(R.string.logged_in_as, username)
                    accountCard.setOnClickListener { showLogoutConfirmDialog() }
                } else {
                    accountCard.visibility = GONE
                }
            }
        }
    }

    private fun setUpSupport() {
        with(binding.staxSupport) {
            twitterContact.setOnClickListener { Utils.openUrl(getString(R.string.stax_twitter_url), requireActivity()) }
            receiveStaxUpdate.setOnClickListener { Utils.openUrl(getString(R.string.receive_stax_updates_url), requireActivity()) }
            requestFeature.setOnClickListener { Utils.openUrl(getString(R.string.stax_nolt_url), requireActivity()) }
            contactSupport.setOnClickListener { Utils.openEmail(getString(R.string.stax_emailing_subject, Hover.getDeviceId(requireActivity())), requireActivity()) }
            faq.setOnClickListener { NavUtil.navigate(findNavController(), SettingsFragmentDirections.actionNavigationSettingsToFaqFragment()) }
        }
    }

    private fun setupLearnCard() {
        with(binding.staxLearn) {
            learnFinances.setOnClickListener { NavUtil.navigate(findNavController(), SettingsFragmentDirections.actionNavigationSettingsToWellnessFragment(null)) }
            learnStax.setOnClickListener { Utils.openUrl(getString(R.string.stax_medium_url), requireActivity()) }
        }
    }

    private fun createDefaultSelector(accounts: List<Account>) {
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

        spinner.setText(defaultAccount?.alias, false)
        spinner.onItemClickListener = OnItemClickListener { _, _, pos: Int, _ -> if (pos != -1) accountsViewModel.setDefaultAccount(accounts[pos]) }
    }

    private fun setUpEnableTestMode() {
        binding.settingsCard.testMode.setOnCheckedChangeListener { _, isChecked ->
            Utils.saveBoolean(Constants.TEST_MODE, isChecked, requireContext())
            UIHelper.flashMessage(requireContext(), if (isChecked) R.string.test_mode_toast else R.string.test_mode_disabled)
        }
        binding.settingsCard.testMode.visibility = if (Utils.getBoolean(Constants.TEST_MODE, requireContext())) VISIBLE else GONE
        binding.disclaimer.setOnClickListener {
            clickCounter++
            if (clickCounter == 5) UIHelper.flashMessage(requireContext(), R.string.test_mode_almost_toast) else if (clickCounter == 7) enableTestMode()
        }
    }

    private fun enableTestMode() {
        Utils.saveBoolean(Constants.TEST_MODE, true, requireActivity())
        binding.settingsCard.testMode.visibility = VISIBLE
        UIHelper.flashMessage(requireContext(), R.string.test_mode_toast)
    }

    private fun startBounties() {
        val navDirection = if (GoogleSignIn.getLastSignedInAccount(requireActivity()) == null)
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
            .setPosButton(R.string.logout) {
                loginViewModel.silentSignOut()
                UIHelper.flashMessage(requireActivity(), getString(R.string.logout_out_success))
            }
        dialog!!.showIt()
    }

    companion object {
        const val SHOW_BOUNTY_LIST = 100
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()

        _binding = null
    }
}