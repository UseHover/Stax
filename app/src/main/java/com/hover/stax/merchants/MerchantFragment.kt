package com.hover.stax.merchants

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentMerchantBinding
import com.hover.stax.hover.AbstractHoverCallerActivity
import com.hover.stax.transfers.AbstractFormFragment
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Utils
import com.hover.stax.utils.collectLifecycleFlow
import com.hover.stax.views.AbstractStatefulInput
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import timber.log.Timber

class MerchantFragment : AbstractFormFragment() {

	private var _binding: FragmentMerchantBinding? = null
	private val binding get() = _binding!!

	private lateinit var viewModel: MerchantViewModel

	@CallSuper
	override fun onCreate(savedInstanceState: Bundle?) {
		abstractFormViewModel = getSharedViewModel<MerchantViewModel>()
		viewModel = abstractFormViewModel as MerchantViewModel
		super.onCreate(savedInstanceState)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentMerchantBinding.inflate(inflater, container, false)
		AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_merchant)), requireActivity())
		accountsViewModel.setType(HoverAction.MERCHANT)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		init(binding.root)
		startObservers(binding.root)
		startListeners()
	}

	override fun startObservers(root: View) {
		super.startObservers(root)
		observeAccountList()
		observeActiveAccount()
		observeActions()
		observeActionSelection()
		observeSelectedMerchant()
		observeAmount()
		observeRecentMerchants()
	}

	private fun observeAccountList() {
		collectLifecycleFlow(accountsViewModel.accountList) {
			if(it.accounts.isEmpty())
				setDropdownTouchListener(MerchantFragmentDirections.actionMerchantFragmentToAccountsFragment())
		}
	}

	private fun observeActiveAccount() {
		accountsViewModel.activeAccount.observe(viewLifecycleOwner) { account ->
			account?.let { binding.summaryCard.accountValue.setTitle(it.toString()) }
			val err = accountsViewModel.errorCheck()
			payWithDropdown.setState(err, if (err == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)
		}
	}

	private fun observeActions() {
		accountsViewModel.channelActions.observe(viewLifecycleOwner) {
			actionSelectViewModel.setActions(it)
		}

		actionSelectViewModel.filteredActions.observe(viewLifecycleOwner) {
			it?.let { if (it.isNotEmpty()) actionSelectViewModel.setActiveAction(it.first()) }
		}
	}

	private fun observeActionSelection() {
		actionSelectViewModel.activeAction.observe(viewLifecycleOwner) {
			it?.let { Timber.i("Updated action to ${it.public_id}") }
		}
	}

	private fun observeRecentMerchants() {
		viewModel.recentMerchants.observe(viewLifecycleOwner) {
			if (!it.isNullOrEmpty()) {
				binding.editCard.merchantSelect.setRecent(it, requireActivity())
				viewModel.merchant.value?.let { m ->
					binding.editCard.merchantSelect.setSelected(m)
				}
			}
		}
	}

	private fun observeSelectedMerchant() {
		viewModel.merchant.observe(viewLifecycleOwner) { it?.let {
			binding.summaryCard.recipientValue.setContent(it.businessName, it.tillNo)
		} }
	}

	private fun observeAmount() {
		viewModel.amount.observe(viewLifecycleOwner) {
			it?.let {
				if (binding.editCard.amountInput.text.isEmpty() && it.isNotEmpty())
					binding.editCard.amountInput.setText(it)
				binding.summaryCard.amountValue.text = Utils.formatAmount(it)
			}
		}
	}

	private fun startListeners() {
		setAmountInputListener()
		setMerchantInputListener()
	}

	private fun setAmountInputListener() {
		binding.editCard.amountInput.apply {
			addTextChangedListener(amountWatcher)
			setOnFocusChangeListener { _, hasFocus ->
				setInputState(hasFocus, this, viewModel.amountErrors())
			}
		}
	}

	private fun setMerchantInputListener() {
		binding.editCard.merchantSelect.apply {
			setSelected(viewModel.merchant.value)
			setAutocompleteClickListener { view, _, position, _ ->
				val merchant = view.getItemAtPosition(position) as Merchant
				viewModel.setMerchant(merchant)
			}
			addTextChangedListener(recipientWatcher)
		}
	}

	override fun onFinishForm() {
		viewModel.saveMerchant()
		viewModel.setEditing(false)
	}

	override fun onSubmitForm() {
		callHover(0)
		findNavController().popBackStack()
	}

	override fun onContactSelected(contact: StaxContact) {
		TODO("Not yet implemented")
	}

	private fun callHover(requestCode: Int) {
		(requireActivity() as AbstractHoverCallerActivity).runSession(payWithDropdown.getHighlightedAccount() ?: accountsViewModel.activeAccount.value!!,
			actionSelectViewModel.activeAction.value!!, getExtras(), requestCode)
	}

	private fun getExtras(): HashMap<String, String> {
		return viewModel.wrapExtras()
	}

	private val amountWatcher: TextWatcher = object : TextWatcher {
		override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
		override fun afterTextChanged(editable: Editable) {}
		override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
			viewModel.setAmount(charSequence.toString().replace(",".toRegex(), ""))
		}
	}

	private val recipientWatcher: TextWatcher = object : TextWatcher {
		override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
		override fun afterTextChanged(editable: Editable) {}
		override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, afterCount: Int) {
			viewModel.setMerchant(charSequence.toString(), payWithDropdown.getHighlightedAccount(), actionSelectViewModel.activeAction.value)
		}
	}

	override fun validates(): Boolean {
		val accountError = accountsViewModel.errorCheck()
		payWithDropdown.setState(accountError, if (accountError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

		val amountError = viewModel.amountErrors()
		binding.editCard.amountInput.setState(amountError, if (amountError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

		val actionError = actionSelectViewModel.errorCheck()

		val recipientError = viewModel.recipientErrors()
		binding.editCard.merchantSelect.setState(recipientError, if (recipientError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

		return accountError == null && actionError == null && amountError == null && recipientError == null
	}

	fun onMerchantSelected(merchant: Merchant) {
		viewModel.setMerchant(merchant)
		binding.editCard.merchantSelect.setSelected(merchant)
	}
}