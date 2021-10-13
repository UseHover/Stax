package com.hover.stax.bounties

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hover.stax.R
import com.hover.stax.databinding.FragmentBountyEmailBinding
import com.hover.stax.navigation.NavigationInterface
import com.hover.stax.utils.Utils.logAnalyticsEvent
import com.hover.stax.utils.Utils.logErrorAndReportToFirebase
import com.hover.stax.utils.Utils.saveString
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class BountyEmailFragment : Fragment(), NavigationInterface, View.OnClickListener {

    private var _binding: FragmentBountyEmailBinding? = null
    private val binding get() = _binding!!
    private var dialog: StaxDialog? = null
    private lateinit var networkMonitor: NetworkMonitor
    private val viewModel: BountyViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBountyEmailBinding.inflate(inflater, container, false)
        networkMonitor = NetworkMonitor(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeEmailResult()

        binding.btnSignIn.apply {
            setSize(SignInButton.SIZE_WIDE)
            setOnClickListener(this@BountyEmailFragment)
        }

        binding.progressIndicator.setVisibilityAfterHide(View.GONE)
        binding.instructions.movementMethod = LinkMovementMethod.getInstance()

        viewModel.user.observe(viewLifecycleOwner) {
            updateProgress(50)
            viewModel.uploadBountyUser(it.email!!, binding.marketingOptIn.isChecked)
        }

        viewModel.didLoginFail.observe(viewLifecycleOwner) {
            if (it == true) {
                updateProgress(100)
                viewModel.setLoginFailed(false)
            }
        }
    }

    override fun onClick(v: View) {
        if (networkMonitor.isNetworkConnected) {
            logAnalyticsEvent(getString(R.string.clicked_bounty_email_continue_btn), requireContext())
            updateProgress(0)
            (activity as BountyActivity).signIn()
        } else {
            showOfflineDialog()
        }
    }

    private fun updateProgress(progress: Int) = with(binding.progressIndicator) {
        when (progress) {
            0 -> show()
            50 -> setProgressCompat(progress, true)
            else -> hide()
        }
    }

    private fun showOfflineDialog() {
        dialog = StaxDialog(requireActivity())
                .setDialogTitle(R.string.internet_required)
                .setDialogMessage(R.string.internet_required_bounty_desc)
                .setPosButton(R.string.btn_ok, null)
                .makeSticky()

        dialog!!.showIt()
    }

    private fun showEdgeCaseErrorDialog() {
        //logout user so that they start the login afresh
        Firebase.auth.signOut()
        updateProgress(100)
        dialog = StaxDialog(requireActivity())
                .setDialogMessage(getString(R.string.edge_case_bounty_email_error))
                .setPosButton(R.string.btn_ok, null)
        dialog!!.showIt()
    }

    private fun observeEmailResult() {
        viewModel.bountyEmailLiveData.observe(viewLifecycleOwner, { responseMap ->
            val entry = responseMap.entries.iterator().next()
            val responseCode = entry.key
            val message = entry.value
            if (responseCode in 200..299) saveAndContinue() else {
                logErrorAndReportToFirebase(TAG, message!!, null)
                if (isAdded && networkMonitor.isNetworkConnected) showEdgeCaseErrorDialog()
            }
        })
    }

    private fun saveAndContinue() {
        updateProgress(100)

        logAnalyticsEvent(getString(R.string.bounty_email_success), requireContext())
        saveString(BountyActivity.EMAIL_KEY, viewModel.user.value!!.email, requireActivity())
        findNavController().navigate(R.id.action_bountyEmailFragment_to_bountyListFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
        _binding = null
    }

    companion object {
        private const val TAG = "BountyEmailFragment"
    }
}