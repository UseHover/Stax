package com.hover.stax.login

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.gms.common.SignInButton
import com.hover.stax.R
import com.hover.stax.databinding.FragmentLoginBinding
import com.hover.stax.settings.ReferralDialog
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

import timber.log.Timber

class LoginDialog : DialogFragment(), View.OnClickListener {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var dialog: StaxDialog
    private lateinit var dialogView: View
    private val loginViewModel: LoginViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.referee_dialog), requireContext())

        networkMonitor = NetworkMonitor(requireContext())
        _binding = FragmentLoginBinding.inflate(layoutInflater)

        dialog = StaxDialog(requireActivity(), binding.root)
            .setDialogTitle(R.string.first_login_dialoghead)
            .setNegButton(R.string.btn_cancel) { dismiss() }

        dialogView = dialog.mView
        return dialog.createIt()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return dialogView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSignIn.apply {
            setSize(SignInButton.SIZE_WIDE)
            setOnClickListener(this@LoginDialog)
        }
        binding.progressIndicator.setVisibilityAfterHide(View.GONE)

        try {
            loginViewModel.username.observe(viewLifecycleOwner) { Timber.i("Loaded username: %s", it ?: "null") }
            loginViewModel.progress.observe(viewLifecycleOwner) { updateProgress(it) }
            loginViewModel.error.observe(viewLifecycleOwner) { it?.let { showError(it) } }
        } catch (e: IllegalArgumentException) {
            Timber.e("Caught an observer in a different lifecycle: $e")
        }

    }

    override fun onClick(v: View?) {
        loginViewModel.error.value = null
        binding.errorText.visibility = View.GONE

        if (networkMonitor.isNetworkConnected) {
            AnalyticsUtil.logAnalyticsEvent(
                getString(R.string.clicked_bounty_email_continue_btn),
                requireContext()
            )
            updateProgress(0)
            startActivityForResult(
                loginViewModel.signInClient.signInIntent,
                AbstractGoogleAuthActivity.LOGIN_REQUEST
            )
        } else showError(getString(R.string.internet_required))
    }

    private fun updateProgress(progress: Int) = with(binding.progressIndicator) {
        when (progress) {
            0 -> show()
            -1 -> hide()
            100 -> {
                hide(); complete()
            }
            else -> setProgressCompat(progress, true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.e("called on activity result")
        if (requestCode == AbstractGoogleAuthActivity.LOGIN_REQUEST)
            loginViewModel.signIntoFirebaseAsync(
                data,
                binding.marketingOptIn.isChecked,
                requireActivity() as AppCompatActivity
            )
    }

    private fun complete() {
        ReferralDialog().show(parentFragmentManager, ReferralDialog.TAG)
        dismiss()
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loginViewModel.error.value = null
        loginViewModel.progress.value = -1
        _binding = null
    }

    companion object {
        const val TAG = "LoginDialog"
    }
}