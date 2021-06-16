package com.hover.stax.balances;

import android.app.Activity;
import android.view.View;

import androidx.navigation.NavController;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.databinding.FragmentBalanceBinding;
import com.hover.stax.home.HomeFragment;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.bubbleshowcase.BubbleShowCase;
import com.hover.stax.utils.bubbleshowcase.BubbleShowCaseListener;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class ShowcaseExecutor implements NavigationInterface {
    private static final String TAG = "ShowcaseExecutor";
    private final Activity activity;
    private final FragmentBalanceBinding balanceBinding;

    public ShowcaseExecutor(Activity a, FragmentBalanceBinding balanceBinding) {
        activity = a;
        this.balanceBinding = balanceBinding;
    }

    private BubbleShowCase startShowcase(String head, String body, BubbleShowCaseListener listener, View view, BubbleShowCase.ArrowPosition arrowPosition, boolean shouldShowOnce) {
        try {
            if (shouldShowOnce)
                return BubbleShowCase.Companion.showcaseOnce(head, body, arrowPosition, listener, view, activity);
            else
                return BubbleShowCase.Companion.showCase(head, body, arrowPosition, listener, view, activity);

        } catch (Exception e) {
            Timber.e(e, "Showcase failed to start");
            return null;
        }
    }

    public BubbleShowCase showcaseAddFirstAccount() {
        return startShowcase(activity.getString(R.string.onboard_addaccounthead), activity.getString(R.string.onboard_addaccountdesc),
                addedAccountListener, (balanceBinding.homeCardBalances.balancesRecyclerView), BubbleShowCase.ArrowPosition.TOP, false);
    }

    public BubbleShowCase showcaseAddSecondAccount() {
        if (balanceBinding.homeCardBalances.balancesRecyclerView.getChildCount() > 0
                && balanceBinding.homeCardBalances.balancesRecyclerView.getChildAt(1) != null) {
            return startShowcase(activity.getString(R.string.onboard_addaccount_greatwork_head), activity.getString(R.string.onboard_addaccount_greatwork_desc),
                    addedAccountListener, (balanceBinding.homeCardBalances.balancesRecyclerView.getChildAt(1).findViewById(R.id.balance_item_card).findViewById(R.id.balance_channel_name)),
                    BubbleShowCase.ArrowPosition.LEFT, true);
        } else return null;
    }


    BubbleShowCaseListener addedAccountListener = new BubbleShowCaseListener() {

        @Override
        public void onBubbleClick(@NotNull BubbleShowCase bubbleShowCase) {
            bubbleShowCase.dismiss();
            goToAddAccountFragment();
        }

        @Override
        public void onBackgroundDimClick(@NotNull BubbleShowCase bubbleShowCase) {
            bubbleShowCase.dismiss();
        }

        @Override
        public void onCloseActionImageClick(@NotNull BubbleShowCase bubbleShowCase) {
            bubbleShowCase.dismiss();
        }

        @Override
        public void onTargetClick(@NotNull BubbleShowCase bubbleShowCase) {
            bubbleShowCase.dismiss();
            goToAddAccountFragment();
        }
    };

    private void goToAddAccountFragment() {
        Amplitude.getInstance().logEvent(activity.getString(R.string.clicked_add_account_bubble));
        HomeFragment.navigateTo(Constants.NAV_LINK_ACCOUNT, activity);
    }
}
