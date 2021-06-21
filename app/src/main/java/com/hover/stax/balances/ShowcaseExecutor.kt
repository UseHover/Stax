package com.hover.stax.balances

import android.app.Activity
import android.view.View
import androidx.cardview.widget.CardView
import com.amplitude.api.Amplitude
import com.hover.stax.R
import com.hover.stax.databinding.FragmentBalanceBinding
import com.hover.stax.home.HomeFragment
import com.hover.stax.navigation.NavigationInterface
import com.hover.stax.utils.Constants
import com.hover.stax.utils.bubbleshowcase.BubbleShowCase
import com.hover.stax.utils.bubbleshowcase.BubbleShowCaseListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber


class ShowcaseExecutor(val activity: Activity, val balanceBinding: FragmentBalanceBinding) : NavigationInterface {

    fun startShowCase(head: String, body: String, listener: BubbleShowCaseListener, view: View,
                      arrowPosition: BubbleShowCase.ArrowPosition, shouldShowOnce: Boolean) {
        try {
            if(shouldShowOnce)
                BubbleShowCase.showcaseOnce(head, body, arrowPosition, listener, view, activity)
            else
                BubbleShowCase.showCase(head, body, arrowPosition, listener, view, activity)
        } catch (e: Exception){
            Timber.e(e, "Showcase failed to start")
        }
    }

    fun showcaseAddFirstAccount(){
        startShowCase(activity.getString(R.string.onboard_addaccounthead), activity.getString(R.string.onboard_addaccountdesc),
            addedAccountListener, (balanceBinding.homeCardBalances.balancesRecyclerView), BubbleShowCase.ArrowPosition.TOP, false)
    }

    private fun goToAddAccountFragment() {
        Amplitude.getInstance().logEvent(activity.getString(R.string.clicked_add_account_bubble))
        HomeFragment.navigateTo(Constants.NAV_LINK_ACCOUNT, activity)
    }

    fun showCaseAddSecondAccount() {
        runBlocking {
            launch {
                delay(2000)

                if(balanceBinding.homeCardBalances.balancesRecyclerView.childCount > 0
                    && balanceBinding.homeCardBalances.balancesRecyclerView.getChildAt(1) != null) {
                    startShowCase(activity.getString(R.string.onboard_addaccount_greatwork_head), activity.getString(R.string.onboard_addaccount_greatwork_desc),
                        addedAccountListener, (balanceBinding.homeCardBalances.balancesRecyclerView.getChildAt(1).findViewById<CardView>(R.id.balance_item_card).findViewById(R.id.balance_channel_name)),
                        BubbleShowCase.ArrowPosition.LEFT, true)
                }
            }
        }
    }

    val addedAccountListener = object: BubbleShowCaseListener {
        override fun onTargetClick(bubbleShowCase: BubbleShowCase) {
            bubbleShowCase.dismiss()
            goToAddAccountFragment()
        }

        override fun onCloseActionImageClick(bubbleShowCase: BubbleShowCase) = bubbleShowCase.dismiss()

        override fun onBackgroundDimClick(bubbleShowCase: BubbleShowCase) = bubbleShowCase.dismiss()

        override fun onBubbleClick(bubbleShowCase: BubbleShowCase) {
            bubbleShowCase.dismiss()
            goToAddAccountFragment()
        }

    }

}