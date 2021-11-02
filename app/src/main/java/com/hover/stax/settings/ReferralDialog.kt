package com.hover.stax.settings

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.hover.stax.R
import com.hover.stax.databinding.StaxReferralDialogBinding
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils.copyToClipboard
import com.hover.stax.utils.Utils.logAnalyticsEvent
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class ReferralDialog : DialogFragment() {

    private var _binding: StaxReferralDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var dialog: StaxDialog
    private lateinit var dialogView: View
    private val viewModel: SettingsViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        logAnalyticsEvent(getString(R.string.referee_dialog), requireContext())
        networkMonitor = NetworkMonitor(requireContext())
        _binding = StaxReferralDialogBinding.inflate(LayoutInflater.from(context))
        dialog = StaxDialog(requireActivity(), binding.root)
            .setDialogTitle(R.string.perm_dialoghead)
            .setDialogMessage(R.string.perm_dialogbody)
            .setNegButton(R.string.btn_cancel) { dismiss() }
            .setPosButton(R.string.btn_save) { attemptSaveReferee() }
            .highlightPos()

        dialogView = dialog.view
        return dialog.createIt()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return dialogView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.progress.value = -1
        binding.refereeInput.addTextChangedListener(refereeWatcher)

        viewModel.username.observe(viewLifecycleOwner) { it?.let { updatePersonalCode(); updateRefereeInfo(viewModel.refereeCode.value) } }
        viewModel.email.observe(viewLifecycleOwner) { Timber.e("got email: %s", it); updatePersonalCode() }
        viewModel.refereeCode.observe(viewLifecycleOwner) { updateRefereeInfo(it) }
        viewModel.error.observe(viewLifecycleOwner) { it?.let { setRefereeState(it, AbstractStatefulInput.ERROR) } }

//        updatePersonalCode()
//        updateRefereeInfo(viewModel.refereeCode.value)
    }

    private val refereeWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (charSequence.isNotEmpty() && viewModel.refereeCode.value != null && charSequence.toString() != viewModel.refereeCode.value)
                setRefereeState(null, AbstractStatefulInput.NONE)
        }
    }

    private fun attemptSaveReferee() {
        if (binding.refereeInput.text.toString().isNullOrEmpty())
            viewModel.error.value = getString(R.string.referral_error_invalid)
        else if (binding.refereeInput.text.toString() == viewModel.username.value)
            viewModel.error.value = getString(R.string.referral_error_self)
        else if (!networkMonitor.isNetworkConnected)
            viewModel.error.value = getString(R.string.referral_error_offline)
        else {
            binding.posBtn.isEnabled = false
            viewModel.saveReferee(binding.refereeInput.text.toString())
        }
    }

    private fun updatePersonalCode() {
        if (!viewModel.username.value.isNullOrEmpty()) {
            binding.referralCode.text = viewModel.username.value
            binding.referralCode.setOnClickListener { copyToClipboard(viewModel.username.value, requireContext()) }
            binding.referralCode.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_copy,0)
        } else {
            binding.referralCode.text = getString(R.string.referral_error_data)
            binding.referralCode.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    private fun updateRefereeInfo(code: String?) {
        if (!viewModel.username.value.isNullOrEmpty() && code.isNullOrEmpty()) {
            binding.posBtn.setOnClickListener { attemptSaveReferee() }
            showForm(true)
        } else if (viewModel.progress.value == 100 && !code.isNullOrEmpty())
            setRefereeState(getString(R.string.saved), AbstractStatefulInput.SUCCESS)
        else
            showForm(false)
    }

    private fun showForm(show: Boolean) {
        binding.vertDivider.visibility = if (show) View.VISIBLE else View.GONE
        binding.refereeInput.visibility = if (show) View.VISIBLE else View.GONE
        binding.userName.visibility = if (show) View.VISIBLE else View.GONE
        binding.userPhone.visibility = if (show) View.VISIBLE else View.GONE
        binding.posBtn.visibility = if (show) View.VISIBLE else View.GONE
        binding.divider.visibility = if (show) View.VISIBLE else View.GONE
        binding.negBtn.text = getString(if (show) R.string.btn_cancel else R.string.btn_ok)
    }

    private fun setRefereeState(msg: String?, type: Int) {
        Timber.e(msg)
        if (type == AbstractStatefulInput.SUCCESS) {
            UIHelper.flashMessage(requireContext(), getString(R.string.saved_referral))
            binding.posBtn.text = getString(R.string.saved)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                binding.posBtn.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.green_state_color))
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.error.value = null
                viewModel.progress.value = -1
                this.dismiss()
            }, 3000)
        } else {
            binding.posBtn.isEnabled = true
            binding.refereeInput.setState(msg, type)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.error.value = null
        viewModel.progress.value = -1
    }

    companion object {
        const val TAG = "ReferralDialog"
    }
}