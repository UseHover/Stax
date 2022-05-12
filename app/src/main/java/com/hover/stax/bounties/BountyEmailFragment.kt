package com.hover.stax.bounties

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.FragmentBountyEmailBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.login.LoginViewModel
import com.hover.stax.settings.SettingsFragment
import com.hover.stax.user.StaxUser
import com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.views.StaxDialog
import com.uxcam.UXCam
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class BountyEmailFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentBountyEmailBinding? = null
    private val binding get() = _binding!!
    private var dialog: StaxDialog? = null
    private lateinit var networkMonitor: NetworkMonitor
    private val loginViewModel: LoginViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBountyEmailBinding.inflate(inflater, container, false)
        networkMonitor = NetworkMonitor(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressIndicator.setVisibilityAfterHide(View.GONE)
        binding.instructions.movementMethod = LinkMovementMethod.getInstance()
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
        if (staxUser != null && !staxUser.isMapper) {
            btnSignIn.visibility = View.GONE
            joinMappers.apply {
                visibility = View.VISIBLE
                setOnClickListener(this@BountyEmailFragment)
            }
        } else {
            joinMappers.visibility = View.GONE
            btnSignIn.apply {
                visibility = View.VISIBLE
                setOnClickListener(this@BountyEmailFragment)
            }
        }
    }

    override fun onClick(v: View) {
        if (networkMonitor.isNetworkConnected) {
            when (v.id) {
                R.id.btnSignIn -> startGoogleSignIn()
                R.id.joinMappers -> joinMappers()
            }
        } else {
            showDialog(R.string.internet_required, getString(R.string.internet_required_bounty_desc), R.string.btn_ok)
        }
    }

    private fun startGoogleSignIn() {
        logAnalyticsEvent(getString(R.string.clicked_bounty_email_continue_btn), requireContext())
        updateProgress(0)
        (activity as MainActivity).signIn()
        loginViewModel.postGoogleAuthNav.value = SettingsFragment.SHOW_BOUNTY_LIST
    }

    private fun joinMappers() {
        updateProgress(0)
        loginViewModel.joinMappers()
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

    private fun complete() = NavUtil.navigate(findNavController(), BountyEmailFragmentDirections.actionBountyEmailFragmentToBountyListFragment())

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