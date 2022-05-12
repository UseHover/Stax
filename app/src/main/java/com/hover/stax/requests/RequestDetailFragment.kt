package com.hover.stax.requests


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentRequestDetailBinding
import com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.UIHelper.flashMessage
import com.hover.stax.utils.Utils
import com.hover.stax.views.Stax2LineItem
import com.hover.stax.views.StaxDialog
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class RequestDetailFragment: Fragment(), RequestSenderInterface  {

    private val viewModel: RequestDetailViewModel by viewModel()
    private val args: RequestDetailFragmentArgs by navArgs()
    private var _binding: FragmentRequestDetailBinding? = null
    private var dialog: StaxDialog? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val data = JSONObject()

        try {
            data.put("id", args.id)
        } catch (ignored: JSONException) {
        }

        logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_request_detail)), data, requireContext())

        _binding = FragmentRequestDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.shareCard.requestLinkCardView.setTitle(getString(R.string.share_again_cardhead))

        viewModel.recipients.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                for (c in it) createRecipientEntry(c)
            }
        }

        viewModel.channel.observe(viewLifecycleOwner) {
            binding.summaryCard.requesterAccountRow.visibility = if (it != null) View.VISIBLE else View.GONE
            it?.let { (view.findViewById(R.id.requesterValue) as Stax2LineItem).setTitle(it.name)  }
        }

        viewModel.request.observe(viewLifecycleOwner) {
            it?.let { setUpSummary(it) }
        }

        viewModel.setRequest(requireArguments().getInt("id"))
        initShareButtons()
    }

    private fun createRecipientEntry(c: StaxContact) {
        val ss2li = Stax2LineItem(requireActivity(), null)
        ss2li.setContact(c)
        binding.summaryCard.requesteeValueList.addView(ss2li)
    }

    private fun setUpSummary(request: Request) {
        binding.summaryCard.requestMoneyCard.setTitle(request.description)
        binding.summaryCard.dateValue.text = DateUtils.humanFriendlyDateTime(request.date_sent)

        if (!request.amount.isNullOrEmpty()) {
            binding.summaryCard.amountRow.visibility = View.VISIBLE
            binding.summaryCard.amountValue.text = Utils.formatAmount(request.amount!!)
        } else
            binding.summaryCard.amountRow.visibility = View.GONE

        if (!request.requester_number.isNullOrEmpty()) binding.summaryCard.requesterValue.setSubtitle(request.requester_number)

        binding.summaryCard.noteRow.visibility = if (request.note.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.summaryCard.noteValue.text = request.note
        binding.cancelBtn.setOnClickListener { showConfirmDialog() }
    }

    private fun showConfirmDialog() {
        if (activity != null) {
            dialog = StaxDialog(requireActivity())
                .setDialogTitle(R.string.cancelreq_head)
                .setDialogMessage(R.string.cancelreq_msg)
                .setNegButton(R.string.btn_back) {}
                .setPosButton(R.string.btn_cancelreq) {
                    viewModel.deleteRequest()
                    flashMessage(requireActivity(), getString(R.string.toast_confirm_cancelreq))
                    NavHostFragment.findNavController(this@RequestDetailFragment).popBackStack()
                }
                .isDestructive

            dialog!!.showIt()
        }
    }

    private fun initShareButtons() {
        if (activity != null) {
            binding.shareCard.smsShareSelection.setOnClickListener { sendSms(viewModel.request.value, viewModel.recipients.value, requireActivity()) }
            binding.shareCard.whatsappShareSelection.setOnClickListener { sendWhatsapp(viewModel.request.value, viewModel.recipients.value, viewModel.channel.value, requireActivity()) }
            binding.shareCard.copylinkShareSelection.setOnClickListener { copyShareLink(viewModel.request.value, binding.shareCard.copylinkShareSelection, requireActivity()) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if(dialog != null && dialog!!.isShowing) dialog!!.dismiss()

        _binding = null
    }

}