package com.hover.stax.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hover.stax.R
import com.hover.stax.databinding.UssdLogBottomsheetBinding
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.UIHelper
import org.koin.androidx.viewmodel.ext.android.viewModel

class USSDLogBottomSheetFragment: BottomSheetDialogFragment() {

	private val viewModel: TransactionDetailsViewModel by viewModel()

	private var _binding: UssdLogBottomsheetBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		val uuid = requireArguments().getString(UUID)
		viewModel.setTransaction(uuid!!)
		_binding = UssdLogBottomsheetBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.closeButton.setOnClickListener { this.dismiss() }
		setCardTitle()
		createUSSDMessagesRecyclerView()
		createSmsMessagesRecyclerView()
	}

	private fun setCardTitle() {
		viewModel.action.observe(viewLifecycleOwner){
			if(it !=null) {
				val type = viewModel.transaction.value!!.fullStatus.getDisplayType(requireContext(), it)
				binding.messagesCard.apply{
					setTitle(getString(R.string.session_fullDesc_cardhead, it.from_institution_name, type ))
					setIcon(getString(R.string.root_url) + it.from_institution_logo)
				}
			}
		}
	}

	private fun createUSSDMessagesRecyclerView() {
		val messagesView = binding.convoRecyclerView
		messagesView.layoutManager = UIHelper.setMainLinearManagers(requireActivity())
		messagesView.setHasFixedSize(true)
		viewModel.messages.observe(viewLifecycleOwner) { updateWithSessionDetails(it, messagesView) }
	}

	private fun createSmsMessagesRecyclerView() {
		val smsView = binding.smsRecyclerView
		smsView.layoutManager = UIHelper.setMainLinearManagers(requireActivity())
		smsView.setHasFixedSize(true)
		viewModel.sms.observe(viewLifecycleOwner) { updateWithSessionDetails(it, smsView) }
	}

	private fun updateWithSessionDetails(messages: List<UssdCallResponse>?, v: RecyclerView) {
		messages?.let {
			val t = viewModel.transaction.value!!
			v.adapter = MessagesAdapter(it,  DateUtils.humanFriendlyDateTime(t.initiated_at), DateUtils.humanFriendlyDateTime(t.updated_at)) }
	}

	companion object {
		const val UUID = "uuid"

		fun newInstance(uuid: String): USSDLogBottomSheetFragment = USSDLogBottomSheetFragment().apply {
			arguments = bundleOf(UUID to uuid)
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

}