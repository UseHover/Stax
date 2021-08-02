package com.hover.stax.bounties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.FragmentBountyEmailBinding
import com.hover.stax.navigation.NavigationInterface
import com.hover.stax.utils.Utils.getString
import com.hover.stax.utils.Utils.logAnalyticsEvent
import com.hover.stax.utils.Utils.logErrorAndReportToFirebase
import com.hover.stax.utils.Utils.saveString
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxDialog
import com.hover.stax.views.StaxTextInputLayout
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class BountyEmailFragment : Fragment(), NavigationInterface, View.OnClickListener {

    private var emailInput: StaxTextInputLayout? = null
    private var binding: FragmentBountyEmailBinding? = null
    private var dialog: StaxDialog? = null
    private val networkMonitor: NetworkMonitor by inject()
    private val viewModel: BountyViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBountyEmailBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeEmailResult()
        emailInput = binding!!.emailInput
        emailInput!!.text = getString(BountyActivity.EMAIL_KEY, requireActivity())
        binding!!.continueEmailBountyButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (networkMonitor.isNetworkConnected) {
            logAnalyticsEvent(getString(R.string.clicked_bounty_email_continue_btn), requireContext())
            if (validates()) {
                emailInput!!.isEnabled = false
                viewModel.uploadBountyUser(emailInput!!.text)
                emailInput!!.setState(getString(R.string.bounty_uploading_email), AbstractStatefulInput.INFO)
            } else {
                emailInput!!.setState(getString(R.string.bounty_email_error), AbstractStatefulInput.ERROR)
            }
        } else {
            showOfflineDialog()
        }
    }

    private fun showOfflineDialog() {
       dialog =  StaxDialog(requireActivity())
                .setDialogTitle(R.string.internet_required)
                .setDialogMessage(R.string.internet_required_bounty_desc)
                .setPosButton(R.string.btn_ok, null)
                .makeSticky();

        dialog!!.showIt()
    }

    private fun showEdgeCaseErrorDialog() {
        dialog = StaxDialog(requireActivity())
                .setDialogMessage(getString(R.string.edge_case_bounty_email_error))
                .setPosButton(R.string.btn_ok, null);
        dialog!!.showIt()
    }

    private fun validates(): Boolean {
        if (emailInput!!.text == null) return false
        val email = emailInput!!.text.replace(" ", "")
        return email.matches("(?:[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])".toRegex())
    }

    private fun observeEmailResult() {
        viewModel.bountyEmailLiveData.observe(viewLifecycleOwner, { responseMap ->
            val entry = responseMap.entries.iterator().next()
            val responseCode = entry.key
            val message = entry.value
            if (responseCode in 200..299) saveAndContinue() else {
                logErrorAndReportToFirebase(TAG, message!!, null)
                if (isAdded && networkMonitor.isNetworkConnected) showEdgeCaseErrorDialog() else setEmailError()
            }
        })
    }

    private fun setEmailError() {
        logAnalyticsEvent(getString(R.string.bounty_email_err, getString(R.string.bounty_api_internet_error)), requireContext())
        emailInput!!.isEnabled = true
        emailInput!!.setState(getString(R.string.bounty_api_internet_error), AbstractStatefulInput.ERROR)
    }

    private fun saveAndContinue() {
        logAnalyticsEvent(getString(R.string.bounty_email_success), requireContext())
        saveString(BountyActivity.EMAIL_KEY, emailInput!!.text, requireActivity())
        findNavController().navigate(R.id.action_bountyEmailFragment_to_bountyListFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
        binding = null
    }

    companion object {
        private const val TAG = "BountyEmailFragment"
    }
}