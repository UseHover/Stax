package com.hover.stax.home;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.hover.stax.R;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.database.Constants;
import com.hover.stax.utils.Utils;
import com.hover.stax.utils.bubbleshowcase.BubbleShowCase;
import com.hover.stax.utils.bubbleshowcase.BubbleShowCaseListener;

import org.jetbrains.annotations.NotNull;

class ShowcaseExecutor {
	public static String SHOW_TUTORIAL = "SHOWCASE";
	private Activity activity;
	private View root;
	private int stage = 0;

	public ShowcaseExecutor(Activity a, View view) {
		activity = a;
		root = view;
	}

	public void startFullOnboardingShowcasing() {
		try {
			BubbleShowCase.Companion.showCase(
					activity.getString(R.string.onboard_sechead),
					activity.getString(R.string.onboard_secbody),
					BubbleShowCase.ArrowPosition.TOP,
					stagedBubbleListener,
					root.findViewById(R.id.home_stax_logo),
					activity);
		} catch (Exception ignored) {
		}
	}

	public void startAddBalanceShowcasing() {
		try {
			BubbleShowCase.Companion.showCase(
					activity.getString(R.string.onboard_addaccounthead),
					activity.getString(R.string.onboard_addaccountbody),
					BubbleShowCase.ArrowPosition.TOP,
					addBalanceListener,
					root.findViewById(R.id.add_accounts_btn),
					activity);
		} catch (Exception igno) {
			Log.d("STAX_TESTING", "IT FAILED HERE");
		}
	}


	private void showcaseSecondStage() {
		openBalance();
		BubbleShowCase.Companion.showCase(
				activity.getString(R.string.onboard_peekhead),
				activity.getString(R.string.onboard_peekbody),
				BubbleShowCase.ArrowPosition.TOP,
				stagedBubbleListener,
				((RecyclerView) root.findViewById(R.id.balances_recyclerView)).getChildAt(0).findViewById(R.id.balance_drag),
				activity);
	}

	private void showcaseThirdStage() {
		openBalance();
		BubbleShowCase.Companion.showCase(
				activity.getString(R.string.onboard_balhead),
				activity.getString(R.string.onboard_balbody),
				BubbleShowCase.ArrowPosition.TOP,
				stagedBubbleListener,
				((RecyclerView) root.findViewById(R.id.balances_recyclerView)).getChildAt(0).findViewById(R.id.balance_drag),
				activity);
	}

	private void showcaseFourthStage() {
		BubbleShowCase.Companion.showCase(
				activity.getString(R.string.onboard_refreshhead),
				activity.getString(R.string.onboard_refreshbody),
				BubbleShowCase.ArrowPosition.TOP,
				stagedBubbleListener,
				root.findViewById(R.id.homeTimeAgo),
				activity);
	}

	private void openBalance() {
		if (root.findViewById(R.id.balances_recyclerView) != null &&
			    ((RecyclerView) root.findViewById(R.id.balances_recyclerView)).getChildCount() > 0 &&
			    ((RecyclerView) root.findViewById(R.id.balances_recyclerView)).getChildAt(0).findViewById(R.id.swipe_reveal_layout) != null)
			((SwipeRevealLayout) ((RecyclerView) root.findViewById(R.id.balances_recyclerView)).getChildAt(0).findViewById(R.id.swipe_reveal_layout)).open(true);
	}

	private void showcaseNextStage(@NotNull BubbleShowCase bubbleShowCase) {
		Utils.saveInt(ShowcaseExecutor.SHOW_TUTORIAL, 1, activity);
		bubbleShowCase.dismiss();
		switch (stage) {
			case 0:
				showcaseSecondStage();
				break;
			case 1:
				showcaseThirdStage();
				break;
			case 2: showcaseFourthStage();
				break;
		}
		stage++;
	}

	BubbleShowCaseListener stagedBubbleListener = new BubbleShowCaseListener() {
		@Override
		public void onTargetClick(@NotNull BubbleShowCase bubbleShowCase) {
			showcaseNextStage(bubbleShowCase);
		}

		@Override
		public void onCloseActionImageClick(@NotNull BubbleShowCase bubbleShowCase) {
			showcaseNextStage(bubbleShowCase);
		}

		@Override
		public void onBackgroundDimClick(@NotNull BubbleShowCase bubbleShowCase) {
		}

		@Override
		public void onBubbleClick(@NotNull BubbleShowCase bubbleShowCase) {
			showcaseNextStage(bubbleShowCase);
		}
	};

	BubbleShowCaseListener addBalanceListener = new BubbleShowCaseListener() {

		@Override
		public void onBubbleClick(@NotNull BubbleShowCase bubbleShowCase) {

		}

		@Override
		public void onBackgroundDimClick(@NotNull BubbleShowCase bubbleShowCase) {

		}

		@Override
		public void onCloseActionImageClick(@NotNull BubbleShowCase bubbleShowCase) {
			goToAddAccountActivity();
			bubbleShowCase.dismiss();
		}

		@Override
		public void onTargetClick(@NotNull BubbleShowCase bubbleShowCase) {
			goToAddAccountActivity();
			bubbleShowCase.dismiss();
		}
	};

	private void goToAddAccountActivity() {
		Amplitude.getInstance().logEvent(activity.getString(R.string.click_add_account));
		activity.startActivityForResult(new Intent(activity, ChannelsActivity.class), Constants.ADD_SERVICE);
	}
}
