package com.hover.stax.paybill

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.databinding.FragmentPaybillBinding
import com.hover.stax.utils.Constants
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class PaybillFragment : Fragment() {

    private var _binding: FragmentPaybillBinding? = null
    private val binding get() = _binding!!

    private val channelsViewModel: ChannelsViewModel by viewModel()
    private val paybillViewModel: PaybillViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaybillBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        channelsViewModel.setType(HoverAction.C2B)

        initListeners()
        startObservers()
        setWatchers()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
        binding.saveBillLayout.saveBill.setOnCheckedChangeListener { _, isChecked ->
            binding.saveBillLayout.saveBillCard.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.billDetailsLayout.businessNoInput.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                channelsViewModel.activeAccount.value?.id?.let {
                    findNavController().navigate(R.id.action_paybillFragment_to_paybillListFragment, bundleOf(Constants.ACCOUNT_ID to it))
                } ?: Timber.e("Active account not set")
                true
            } else
                false
        }

        binding.continueBtn.setOnClickListener { }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun startObservers() {
        paybillViewModel.selectedPaybill.observe(viewLifecycleOwner) {
            binding.billDetailsLayout.businessNoInput.text = it.name
        }

        with(channelsViewModel) {
            binding.billDetailsLayout.accountDropdown.apply {
                setListener(this@with)
                setObservers(this@with, viewLifecycleOwner)
            }

            setupActionDropdownObservers(this, viewLifecycleOwner)

            accounts.observe(viewLifecycleOwner) {
                if (it.isEmpty())
                    binding.billDetailsLayout.accountDropdown.autoCompleteTextView.setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_DOWN)
                            findNavController().navigate(R.id.action_paybillFragment_to_accountsFragment)
                        true
                    }
            }
        }
    }

    private fun setupActionDropdownObservers(viewModel: ChannelsViewModel, lifecycleOwner: LifecycleOwner) {

        val activeChannelObserver = object : Observer<Channel> {
            override fun onChanged(t: Channel?) {
                Timber.i("Got new active channel: $t ${t?.countryAlpha2}")
            }
        }

        val actionsObserver = object : Observer<List<HoverAction>> {
            override fun onChanged(t: List<HoverAction>?) {
                Timber.i("Got new actions: %s", t?.size)
            }
        }

        viewModel.activeChannel.observe(lifecycleOwner, activeChannelObserver)
        viewModel.channelActions.observe(lifecycleOwner, actionsObserver)
    }

    private fun setWatchers() = with(binding.billDetailsLayout) {
        businessNoInput.addTextChangedListener(businessNoWatcher)
        accountNoInput.addTextChangedListener(accountNoWatcher)
        amountInput.addTextChangedListener(amountWatcher)
    }

    private val amountWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            paybillViewModel.setAmount(charSequence.toString().replace(",".toRegex(), ""))
        }
    }

    private val businessNoWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            paybillViewModel.setBusinessNumber(charSequence.toString())
        }
    }

    private val accountNoWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            paybillViewModel.setAccountNumber(charSequence.toString().replace(",".toRegex(), ""))
        }
    }

}
