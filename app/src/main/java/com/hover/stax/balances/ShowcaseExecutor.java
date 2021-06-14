package com.hover.stax.balances;

import android.app.Activity;
import android.view.View;

import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;
import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.databinding.FragmentBalanceBinding;
import com.hover.stax.databinding.FragmentMainBinding;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.utils.bubbleshowcase.BubbleShowCase;
import com.hover.stax.utils.bubbleshowcase.BubbleShowCaseListener;

import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

public class ShowcaseExecutor implements NavigationInterface {
	private static final String TAG = "ShowcaseExecutor";
	private final Activity activity;
	private final NavController navController;
	private final FragmentBalanceBinding balanceBinding;

	public ShowcaseExecutor(Activity a, NavController navController, FragmentBalanceBinding balanceBinding) {
		activity = a;
		this.balanceBinding = balanceBinding;
		this.navController = navController;
	}
	public void forceDismiss() {

	}

	private void startShowcase(String head, String body, BubbleShowCaseListener listener, View view) {
		try {
			BubbleShowCase.Companion.showCase(head, body, BubbleShowCase.ArrowPosition.TOP, listener, view, activity);
		} catch (Exception e) { Timber.e(e, "Showcase failed to start"); }
	}

	public void showcaseAddAccount(int title, int desc) {
		startShowcase(activity.getString(title), activity.getString(desc),
				addedAccountListener, (balanceBinding.homeCardBalances.balancesRecyclerView));
	}

	BubbleShowCaseListener addedAccountListener = new BubbleShowCaseListener() {

		@Override public void onBubbleClick(@NotNull BubbleShowCase bubbleShowCase) {
			bubbleShowCase.dismiss();
			goToAddAccountFragment();
		}

		@Override
		public void onBackgroundDimClick(@NotNull BubbleShowCase bubbleShowCase) { bubbleShowCase.dismiss(); }

		@Override
		public void onCloseActionImageClick(@NotNull BubbleShowCase bubbleShowCase) { bubbleShowCase.dismiss(); }

		@Override
		public void onTargetClick(@NotNull BubbleShowCase bubbleShowCase) {
			bubbleShowCase.dismiss();
			goToAddAccountFragment();
		}
	};

	private void goToAddAccountFragment() {
		Amplitude.getInstance().logEvent(activity.getString(R.string.clicked_add_account_bubble));
		navigateToChannelsListFragment(navController, true);
	}
}
