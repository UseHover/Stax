package com.hover.stax.bounties

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.SignInButton
import com.hover.stax.R
import com.hover.stax.databinding.FragmentBountyEmailBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.navigation.NavigationInterface
import com.hover.stax.settings.SettingsViewModel
import com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class BountyEmailFragment : Fragment(), NavigationInterface, View.OnClickListener {

    private var _binding: FragmentBountyEmailBinding? = null
    private val binding get() = _binding!!
    private var dialog: StaxDialog? = null
    private lateinit var networkMonitor: NetworkMonitor
    private val settingsViewModel: SettingsViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBountyEmailBinding.inflate(inflater, container, false)
        networkMonitor = NetworkMonitor(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignIn.apply {
            setSize(SignInButton.SIZE_WIDE)
            setOnClickListener(this@BountyEmailFragment)
        }

        binding.progressIndicator.setVisibilityAfterHide(View.GONE)
        binding.instructions.movementMethod = LinkMovementMethod.getInstance()
        startObservers()
    }

    private fun startObservers() {
        with(settingsViewModel) {
            val emailObserver = object : Observer<String?> {
                override fun onChanged(t: String?) {
                    Timber.e("Got email from Google $t")
                }
            }

            val usernameObserver = object : Observer<String?> {
                override fun onChanged(t: String?) {
                    Timber.e("Got username : $t")
                }
            }

            progress.observe(viewLifecycleOwner) { updateProgress(it) }
            error.observe(viewLifecycleOwner) { it?.let { showError(it) } }
            email.observe(viewLifecycleOwner, emailObserver)
            username.observe(viewLifecycleOwner, usernameObserver)
        }
    }

    override fun onClick(v: View) {
        if (networkMonitor.isNetworkConnected) {
            logAnalyticsEvent(getString(R.string.clicked_bounty_email_continue_btn), requireContext())
            updateProgress(0)
            (activity as MainActivity).signIn()
        } else {
            showDialog(R.string.internet_required, getString(R.string.internet_required_bounty_desc), R.string.btn_ok)
        }
    }

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

    private fun complete() = findNavController().navigate(R.id.action_bountyEmailFragment_to_bountyListFragment)

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