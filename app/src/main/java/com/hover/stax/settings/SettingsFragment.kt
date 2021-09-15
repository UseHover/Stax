package com.hover.stax.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.hover.sdk.api.Hover
import com.hover.stax.BuildConfig
import com.hover.stax.R
import com.hover.stax.account.Account
import com.hover.stax.databinding.FragmentSettingsBinding
import com.hover.stax.languages.LanguageViewModel
import com.hover.stax.library.LibraryActivity
import com.hover.stax.navigation.NavigationInterface
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class SettingsFragment : Fragment(), NavigationInterface {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var accountAdapter: ArrayAdapter<Account>? = null
    private var clickCounter = 0

    private val viewModel: PinsViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_security)), requireActivity())

        setUpAccounts(viewModel);
        setUpChooseLang();
        setUpContactStax();
        setUpUssdLibrary();
        setupRequestFeature();
        setUpEnableTestMode();
        setupFaq();
        setupAppVersionInfo();
    }

    private fun setUpAccounts(viewModel: PinsViewModel) {
        accountAdapter = ArrayAdapter(requireActivity(), R.layout.stax_spinner_item)
        viewModel.accounts.observe(viewLifecycleOwner) {
            showAccounts(it)

            if (!it.isNullOrEmpty() && it.size > 1)
                createDefaultSelector(it)
            else
                binding.cardAccounts.defaultAccountEntry.visibility = GONE
        }
    }

    private fun setUpChooseLang() {
        val selectLangBtn = binding.languageCard.selectLanguageBtn
        val languageVM = getViewModel<LanguageViewModel>()
        languageVM.loadLanguages().observe(viewLifecycleOwner) { langs ->
            langs.forEach {
                if (it.isSelected) selectLangBtn.text = it.name
            }
        }

        selectLangBtn.setOnClickListener { navigateToLanguageSelectionFragment(requireActivity()) }
    }

    private fun setupAppVersionInfo() {
        val deviceId = Hover.getDeviceId(requireContext())
        val appVersion: String = BuildConfig.VERSION_NAME
        val versionCode: String = java.lang.String.valueOf(BuildConfig.VERSION_CODE)
        binding.staxAndDeviceInfo.text = getString(R.string.app_version_and_device_id, appVersion, versionCode, deviceId)
    }

    private fun setUpContactStax() {
        with(binding.contactStax) {
            twitterContact.setOnClickListener { Utils.openUrl(getString(R.string.stax_twitter_url), requireActivity()) }
            receiveStaxUpdate.setOnClickListener { Utils.openUrl(getString(R.string.receive_stax_updates_url), requireActivity()) }
        }
    }

    private fun setupRequestFeature() = binding.getSupportStax.requestFeature.setOnClickListener {
        Utils.openUrl(getString(R.string.stax_nolt_url), requireActivity())
    }

    private fun setupFaq() = binding.getSupportStax.faq.setOnClickListener { navigateFAQ(this) }

    private fun setUpUssdLibrary() = binding.libraryCard.visitLibrary.setOnClickListener {
        requireActivity().startActivity(Intent(requireActivity(), LibraryActivity::class.java))
    }

    private fun showAccounts(accounts: List<Account>) {
        val lv = binding.cardAccounts.accountsList
        accountAdapter!!.clear()
        accountAdapter!!.addAll(accounts)
        lv.adapter = accountAdapter
        lv.setOnItemClickListener { _, _, position, _ -> navigateToPinUpdateFragment(accounts[position].id, this@SettingsFragment) }
        UIHelper.fixListViewHeight(lv)
    }

    private fun createDefaultSelector(accounts: List<Account>) {
        val spinner = binding.cardAccounts.defaultAccountSpinner
        binding.cardAccounts.defaultAccountEntry.visibility = VISIBLE
        spinner.setAdapter(accountAdapter)
        spinner.setText(accounts.first { it.isDefault }.alias, false);
        spinner.onItemClickListener = OnItemClickListener { _, _, pos: Int, _ -> if (pos != 0) viewModel.setDefaultAccount(accounts[pos]) }
    }

    private fun setUpEnableTestMode() {
        binding.contactStax.testMode.setOnCheckedChangeListener { _, isChecked ->
            Utils.saveBoolean(Constants.TEST_MODE, isChecked, requireContext())
            UIHelper.flashMessage(requireContext(), if (isChecked) R.string.test_mode_toast else R.string.test_mode_disabled)
        }
        binding.contactStax.testMode.visibility = if (Utils.getBoolean(Constants.TEST_MODE, requireContext())) VISIBLE else GONE
        binding.disclaimer.setOnClickListener {
            clickCounter++
            if (clickCounter == 5) UIHelper.flashMessage(requireContext(), R.string.test_mode_almost_toast) else if (clickCounter == 7) enableTestMode()
        }
    }

    private fun enableTestMode() {
        Utils.saveBoolean(Constants.TEST_MODE, true, requireActivity())
        binding.contactStax.testMode.visibility = VISIBLE
        UIHelper.flashMessage(requireContext(), R.string.test_mode_toast)
    }

    companion object {
        @JvmField
        val LANG_CHANGE = "Settings"
    }
}