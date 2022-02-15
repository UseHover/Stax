package com.hover.stax.settings

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.hover.stax.R
import com.hover.stax.databinding.StaxReferralDialogBinding
import com.hover.stax.login.LoginViewModel
import com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils.copyToClipboard
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class ReferralDialog : DialogFragment() {

    private var _binding: StaxReferralDialogBinding? = null
    private val binding get() = _binding!!
    private var retryUploadingCalled = false

    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var dialog: StaxDialog
    private lateinit var dialogView: View
    private val loginViewModel: LoginViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        logAnalyticsEvent(getString(R.string.referee_dialog), requireContext())
        networkMonitor = NetworkMonitor(requireContext())

        _binding = StaxReferralDialogBinding.inflate(layoutInflater)

        dialog = StaxDialog(requireActivity(), binding.root)
            .setDialogTitle(R.string.perm_dialoghead)
            .setDialogMessage(R.string.perm_dialogbody)
            .setNegButton(R.string.btn_cancel) { dismiss() }
            .setPosButton(R.string.btn_save) { attemptSaveReferee() }
            .highlightPos()

        dialogView = dialog.mView
        return dialog.createIt()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return dialogView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loginViewModel.progress.value = -1
        binding.refereeInput.addTextChangedListener(refereeWatcher)
        binding.nameInput.addTextChangedListener(nameWatcher)
        binding.phoneInput.addTextChangedListener(phoneWatcher)

        loginViewModel.username.observe(viewLifecycleOwner) { it?.let { updatePersonalCode(); updateRefereeInfo(loginViewModel.refereeCode.value) } }
        loginViewModel.email.observe(viewLifecycleOwner) { Timber.e("got email: %s", it); updatePersonalCode() }
        loginViewModel.refereeCode.observe(viewLifecycleOwner) { updateRefereeInfo(it) }
        loginViewModel.error.observe(viewLifecycleOwner) { it?.let { showError(it) } }
    }

    private val refereeWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (charSequence.isNotEmpty() && loginViewModel.refereeCode.value != null && charSequence.toString() != loginViewModel.refereeCode.value)
                setRefereeState(null, AbstractStatefulInput.NONE)
        }
    }

    private val nameWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (charSequence.isNotEmpty())
                binding.nameInput.setState(null, AbstractStatefulInput.NONE)
        }
    }

    private val phoneWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (charSequence.isNotEmpty())
                binding.phoneInput.setState(null, AbstractStatefulInput.NONE)
        }
    }

    private fun attemptSaveReferee() {
        when {
            binding.refereeInput.text.isEmpty() -> binding.refereeInput.setError(getString(R.string.referral_error_invalid))
            binding.refereeInput.text == loginViewModel.username.value -> binding.refereeInput.setError(getString(R.string.referral_error_self))
            binding.nameInput.text.isEmpty() -> binding.nameInput.setError(getString(R.string.referral_error_name))
            binding.phoneInput.text.isEmpty() -> binding.phoneInput.setError(getString(R.string.referral_error_phone))
            binding.refereeInput.text == loginViewModel.username.value -> binding.refereeInput.setError(getString(R.string.referral_error_self))
            !networkMonitor.isNetworkConnected -> loginViewModel.error.value = getString(R.string.referral_error_offline)
            else -> {
                binding.posBtn.isEnabled = false
                loginViewModel.saveReferee(binding.refereeInput.text, binding.nameInput.text, binding.phoneInput.text)
            }
        }
    }

    private fun updatePersonalCode() {
        if (!loginViewModel.username.value.isNullOrEmpty()) {
            binding.referralCode.text = loginViewModel.username.value
            binding.referralCode.setOnClickListener { copyToClipboard(loginViewModel.username.value, requireContext()) }
            binding.referralCode.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_copy, 0)
        } else {
            retryUploadingUserInfo()
        }
    }

    private fun retryUploadingUserInfo() {
        if (!retryUploadingCalled) {
            retryUploadingCalled = true
            loginViewModel.uploadLastUser()
        }
    }

    private fun updateRefereeInfo(code: String?) {
        if (!loginViewModel.username.value.isNullOrEmpty() && code.isNullOrEmpty()) {
            binding.posBtn.setOnClickListener { attemptSaveReferee() }
            showForm(true)
        } else if (loginViewModel.progress.value == 100 && !code.isNullOrEmpty())
            setRefereeState(getString(R.string.label_saved), AbstractStatefulInput.SUCCESS)
        else
            showForm(false)
    }

    private fun showForm(show: Boolean) {
        binding.vertDivider.visibility = if (show) View.VISIBLE else View.GONE
        binding.refereeInput.visibility = if (show) View.VISIBLE else View.GONE
        binding.nameInput.visibility = if (show) View.VISIBLE else View.GONE
        binding.phoneInput.visibility = if (show) View.VISIBLE else View.GONE
        binding.posBtn.visibility = if (show) View.VISIBLE else View.GONE
        binding.divider.visibility = if (show) View.VISIBLE else View.GONE
        binding.negBtn.text = getString(if (show) R.string.btn_cancel else R.string.btn_ok)
    }

    private fun setRefereeState(msg: String?, type: Int) {
        Timber.e(msg)
        if (type == AbstractStatefulInput.SUCCESS) {
            UIHelper.flashMessage(requireContext(), getString(R.string.saved_referral))
            binding.posBtn.text = getString(R.string.label_saved)

            binding.posBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.stax_state_green))

            lifecycleScope.launch {
                delay(1000)
                this@ReferralDialog.dismiss()
            }
        } else {
            binding.posBtn.isEnabled = true
            binding.refereeInput.setState(msg, type)
        }
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE

//        binding.referralCode.text = getString(R.string.referral_error_try_again)
//        binding.referralCode.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loginViewModel.error.value = null
        loginViewModel.progress.value = -1
        _binding = null
    }

    companion object {
        const val TAG = "ReferralDialog"
    }
}