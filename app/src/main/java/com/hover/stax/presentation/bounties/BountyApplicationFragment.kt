package com.hover.stax.presentation.bounties

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.FragmentBountyApplicationBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.login.LoginViewModel
import com.hover.stax.user.StaxUser
import com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class BountyApplicationFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentBountyApplicationBinding? = null
    private val binding get() = _binding!!
    private var dialog: StaxDialog? = null
    private lateinit var networkMonitor: NetworkMonitor
    private val loginViewModel: LoginViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
            progress.observe(viewLifecycleOwner) { updateProgress(it) }
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