package com.hover.stax.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.hover.sdk.transactions.TransactionContract
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.databinding.FragmentChannelBinding
import com.hover.stax.futureTransactions.FutureViewModel
import com.hover.stax.futureTransactions.RequestsAdapter
import com.hover.stax.futureTransactions.ScheduledAdapter
import com.hover.stax.home.MainActivity
import com.hover.stax.navigation.NavigationInterface
import com.hover.stax.requests.Request
import com.hover.stax.schedules.Schedule
import com.hover.stax.transactions.TransactionHistoryAdapter
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.AbstractStatefulInput
import org.koin.androidx.viewmodel.ext.android.viewModel


class AccountDetailFragment : Fragment(), TransactionHistoryAdapter.SelectListener, ScheduledAdapter.SelectListener,
    RequestsAdapter.SelectListener, NavigationInterface {

    private val viewModel: AccountDetailViewModel by viewModel()
    private val futureViewModel: FutureViewModel by viewModel()

    private var requestsAdapter: RequestsAdapter? = null
    private var scheduledAdapter: ScheduledAdapter? = null

    private var _binding: FragmentChannelBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChannelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_channel)), requireActivity());

        initRecyclerViews()
        setupObservers()

        binding.refreshBalanceBtn.setOnClickListener { onRefresh() }
        binding.renameAcctBtn.setOnClickListener { startRenameFlow() }

        arguments?.let { viewModel.setChannel(it.getInt(TransactionContract.COLUMN_CHANNEL_ID)) }
    }

    private fun setupObservers() {
        with(viewModel) {
            channel.observe(viewLifecycleOwner) {
                binding.staxCardView.setTitle(it.name)
                binding.feesDescription.text = getString(R.string.fees_label, it.name)
                binding.detailsBalance.text = it.latestBalance
                binding.originalName.text = it.name

                setUpFuture(it)
            }

            val txHistoryRv = binding.homeCardTransactions.transactionHistoryRecyclerView
            transactions.observe(viewLifecycleOwner) {
                binding.homeCardTransactions.noHistory.visibility = if (it.isNullOrEmpty()) View.VISIBLE else View.GONE
                txHistoryRv.apply {
                    layoutManager = UIHelper.setMainLinearManagers(requireActivity())
                    adapter = TransactionHistoryAdapter(it, this@AccountDetailFragment)
                }
            }

            spentThisMonth.observe(viewLifecycleOwner) { binding.detailsMoneyOut.text = Utils.formatAmount(it ?: 0.0) }
            feesThisYear.observe(viewLifecycleOwner) { binding.detailsFees.text = Utils.formatAmount(it ?: 0.0) }
        }
    }

    private fun initRecyclerViews() {
        binding.scheduledCard.scheduledRecyclerView.apply {
            layoutManager = UIHelper.setMainLinearManagers(context)
            scheduledAdapter = ScheduledAdapter(null, this@AccountDetailFragment)
            adapter = scheduledAdapter
        }

        binding.scheduledCard.requestsRecyclerView.apply {
            layoutManager = UIHelper.setMainLinearManagers(context)
            requestsAdapter = RequestsAdapter(null, this@AccountDetailFragment)
            adapter = requestsAdapter
        }
    }

    private fun setUpFuture(channel: Channel) {
        with(futureViewModel) {
            scheduledByChannel(channel.id).observe(viewLifecycleOwner) {
                scheduledAdapter?.updateData(it)
                setFutureVisible(it, requests.value)
            }

            requestsByChannel(channel.id).observe(viewLifecycleOwner) {
                requestsAdapter?.updateData(it)
                setFutureVisible(scheduled.value, it)
            }
        }
    }

    private fun setFutureVisible(schedules: List<Schedule>?, requests: List<Request>?) {
        val visible = !schedules.isNullOrEmpty() || !requests.isNullOrEmpty()
        binding.scheduledCard.root.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun onRefresh() = viewModel.channel.value?.let { (activity as MainActivity).onTapRefresh(it.id) }

    override fun viewRequestDetail(id: Int) {
        navigateToRequestDetailsFragment(id, this)
    }

    override fun viewScheduledDetail(id: Int) {
        navigateToScheduleDetailsFragment(id, this)
    }

    override fun viewTransactionDetail(uuid: String?) {
        NavHostFragment.findNavController(this).navigate(R.id.transactionDetailsFragment, bundleOf(TransactionContract.COLUMN_UUID to uuid))
    }

    private fun startRenameFlow() {
        toggleAccountDetails(true)

        val channel = viewModel.channel.value
        binding.renameCard.currentName.text = channel?.name
        binding.renameCard.btnSubmit.setOnClickListener { updateAccountName() }

        binding.renameCard.root.setOnClickIcon { toggleAccountDetails(false) }

        handleBackPress()
    }

    private fun updateAccountName() {
        if (validates())
            viewModel.updateAccountName()
    }

    private fun validates(): Boolean {
        val error = viewModel.newNameError()
        binding.renameCard.newName.setState(error, if (error == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        return error == null
    }

    private fun toggleAccountDetails(hide: Boolean) {
        with(binding) {
            staxCardView.visibility = if (hide) View.GONE else View.VISIBLE
            homeCardTransactions.root.visibility = if (hide) View.GONE else View.VISIBLE
            renameCard.renameAccountCard.visibility = if (hide) View.VISIBLE else View.GONE
        }
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.renameCard.root.visibility == View.VISIBLE)
                    toggleAccountDetails(false)
                else
                    requireActivity().onBackPressed()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}