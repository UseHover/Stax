package com.hover.stax.settings

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.SignInButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.hover.stax.R
import com.hover.stax.bounties.BountyActivity
import com.hover.stax.databinding.FragmentLoginBinding
import com.hover.stax.databinding.StaxReferralDialogBinding
import com.hover.stax.utils.Utils
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class LoginDialog: DialogFragment(), View.OnClickListener {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var dialog: StaxDialog
    private lateinit var dialogView: View
    private val viewModel: SettingsViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Utils.logAnalyticsEvent(getString(R.string.referee_dialog), requireContext())
        networkMonitor = NetworkMonitor(requireContext())
        _binding = FragmentLoginBinding.inflate(LayoutInflater.from(context))
        dialog = StaxDialog(requireActivity(), binding.root).setDialogTitle(R.string.first_login_dialoghead).setNegButton(R.string.btn_cancel) { dismiss() }

        dialogView = dialog.view
        return dialog.createIt()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return dialogView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSignIn.apply {
            setSize(SignInButton.SIZE_WIDE)
            setOnClickListener(this@LoginDialog)
        }
        binding.progressIndicator.setVisibilityAfterHide(View.GONE)
        viewModel.createGoogleClient(requireActivity() as AppCompatActivity)

        viewModel.username.observe(this) { Timber.e("Loaded username: %s", it) }
        viewModel.progress.observe(viewLifecycleOwner) { updateProgress(it) }
        viewModel.error.observe(viewLifecycleOwner) { it?.let { showError(it) } }
    }

    override fun onClick(v: View?) {
        viewModel.error.value = null
        binding.errorText.visibility = View.GONE
        if (networkMonitor.isNetworkConnected) {
            Utils.logAnalyticsEvent(getString(R.string.clicked_bounty_email_continue_btn), requireContext())
            updateProgress(0)
            startActivityForResult(viewModel.signInClient.signInIntent, SettingsViewModel.LOGIN_REQUEST)
        } else
            showError(getString(R.string.internet_required))
    }

    private fun updateProgress(progress: Int) = with(binding.progressIndicator) {
        when (progress) {
            0 -> show()
            -1 -> { hide() }
            100 -> { hide(); complete() }
            else -> setProgressCompat(progress, true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.e("called on activity result")
        if (requestCode == SettingsViewModel.LOGIN_REQUEST)
            viewModel.signIntoFirebaseAsync(data, binding.marketingOptIn.isChecked, requireActivity() as AppCompatActivity)
    }

    private fun complete() {
        ReferralDialog().show(parentFragmentManager, ReferralDialog.TAG)
        dismiss()
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.error.value = null
        viewModel.progress.value = -1
    }

    companion object {
        const val TAG = "LoginDialog"
    }
}