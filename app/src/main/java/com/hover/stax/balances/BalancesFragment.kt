package com.hover.stax.balances

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.databinding.FragmentBalanceBinding
import com.hover.stax.home.HomeFragment
import com.hover.stax.home.MainActivity
import com.hover.stax.navigation.NavigationInterface
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.staxcardstack.StaxCardStackView
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class BalancesFragment : Fragment(), NavigationInterface {

    private lateinit var addChannelLink: CardView
    private lateinit var balanceTitle: TextView
    private lateinit var balanceStack: StaxCardStackView
    private lateinit var balancesRecyclerView: RecyclerView

    private var balancesVisible = false
    private var channelList: List<Channel>? = null

    private var _binding: FragmentBalanceBinding? = null
    private val binding get() = _binding!!

    private val balancesViewModel: BalancesViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentBalanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        balanceStack = binding.stackBalanceCards
        setUpBalances()
        setUpLinkNewAccount()
    }

    private fun setUpBalances() {
        initBalanceCard()
        balancesViewModel.selectedChannels.observe(viewLifecycleOwner, ::updateServices)
    }

    private fun setUpLinkNewAccount() {
        addChannelLink = binding.newAccountLink
        addChannelLink.setOnClickListener { HomeFragment.navigateTo(Constants.NAV_LINK_ACCOUNT, requireActivity()) }
    }

    private fun initBalanceCard() {
        balanceTitle = binding.homeCardBalances.balanceHeaderTitleId.also {
            it.setCompoundDrawablesRelativeWithIntrinsicBounds(
                if (balancesVisible) R.drawable.ic_visibility_on else R.drawable.ic_visibility_off, 0, 0, 0
            )
            it.setOnClickListener {
                showBalanceCards(!balancesVisible)
                showBubbleIfRequired()
            }
        }

        balancesRecyclerView = binding.homeCardBalances.balancesRecyclerView.also {
            it.layoutManager = UIHelper.setMainLinearManagers(context)
            it.setHasFixedSize(true)
        }
    }

    private fun showBalanceCards(status: Boolean) {
        toggleLink(status)
        balanceTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (status) R.drawable.ic_visibility_on else R.drawable.ic_visibility_off, 0, 0, 0
        )

        if (status) {
            balanceStack.visibility = View.GONE
            binding.homeCardBalances.balancesMl.transitionToEnd()
        } else {
            balanceStack.visibility = View.VISIBLE
            binding.homeCardBalances.balancesMl.transitionToStart()
        }

        balancesVisible = status
    }

    private fun updateServices(channels: List<Channel>) {
        SHOW_ADD_ANOTHER_ACCOUNT = !channels.isNullOrEmpty() && !Channel.hasDummy(channels)
        addDummyChannelsIfRequired(channels)

        val balancesAdapter = BalanceAdapter(channels, activity as MainActivity)
        balancesRecyclerView.adapter = balancesAdapter
        balancesAdapter.showBalanceAmounts(true)

        showBalanceCards(Channel.areAllDummies(channels))
        updateStackCard(channels)

        channelList = channels
        showBubbleIfRequired()
    }

    private fun showBubbleIfRequired() {
        channelList?.let {
            if (Channel.areAllDummies(it)) {
                if (!SHOWN_BUBBLE_MAIN_ACCOUNT && balancesVisible) {
                    ShowcaseExecutor(requireActivity(), binding).showcaseAddFirstAccount()
                    SHOWN_BUBBLE_MAIN_ACCOUNT = true
                }
            } else if (Channel.hasDummy(channelList)) {
                if (!SHOWN_BUBBLE_OTHER_ACCOUNT && balancesVisible) {
                    ShowcaseExecutor(requireActivity(), binding).showCaseAddSecondAccount()
                    SHOWN_BUBBLE_OTHER_ACCOUNT = true
                }
            }
        }
    }

    private fun updateStackCard(channels: List<Channel>?) {
        channels?.let {
            val temp = channels.reversed()

            val cardStackAdapter = BalanceCardStackAdapter(requireActivity())
            balanceStack.setAdapter(cardStackAdapter)
            cardStackAdapter.updateData(temp)

            balanceStack.apply {
                setOverlapGaps(STACK_OVERLAY_GAP)
                rotationX = ROTATE_UPSIDE_DOWN
                setOnClickListener { showBalanceCards(!balancesVisible) }
            }

            updateBalanceCardStackHeight(temp.size)
        }
    }

    private fun updateBalanceCardStackHeight(numOfItems: Int) {
        val params = balanceStack.layoutParams
        params.height = 20 * numOfItems
        balanceStack.layoutParams = params
    }

    private fun addDummyChannelsIfRequired(channels: List<Channel>?) {
        channels?.let {
            if (it.isEmpty()) {
                channels.toMutableList().add(Channel().dummy(getString(R.string.your_main_account), GREEN_BG))
                channels.toMutableList().add(Channel().dummy(getString(R.string.your_other_account), BLUE_BG))
            }
            if (it.size == 1) {
                channels.toMutableList().add(Channel().dummy(getString(R.string.your_other_account), BLUE_BG))
            }
        }
    }

    private fun toggleLink(show: Boolean) {
        if (SHOW_ADD_ANOTHER_ACCOUNT) addChannelLink.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val GREEN_BG = "#46E6CC"
        const val BLUE_BG = "#04CCFC"

        const val STACK_OVERLAY_GAP = 10
        const val ROTATE_UPSIDE_DOWN = 180f

        private var SHOW_ADD_ANOTHER_ACCOUNT = false
        private var SHOWN_BUBBLE_MAIN_ACCOUNT = false
        private var SHOWN_BUBBLE_OTHER_ACCOUNT = false
    }
}