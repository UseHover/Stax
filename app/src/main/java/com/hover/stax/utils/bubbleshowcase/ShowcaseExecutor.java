package com.hover.stax.utils.bubbleshowcase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.hover.sdk.utils.Constants;
import com.hover.sdk.utils.Utils;
import com.hover.stax.R;

import org.jetbrains.annotations.NotNull;

public class ShowcaseExecutor {
	private static String TAG = "ShowcaseExecutor";
	public static String SHOW_TUTORIAL = "SHOWCASE";
	private Activity activity;
	private View root;
	private int stage = 0;

	public ShowcaseExecutor(Activity a, View view) {
		activity = a;
		root = view;
	}

	private void startShowcase(String head, String body, BubbleShowCaseListener listener, View view) {
		try {
			BubbleShowCase.Companion.showCase(
					head, body, BubbleShowCase.ArrowPosition.TOP, listener, view, activity);
		} catch (Exception e) { Log.e(TAG, "Showcase failed to start", e); }
	}

	public void showcaseAddAcctStage() {
		startShowcase(activity.getString(R.string.onboard_addaccounthead), activity.getString(R.string.onboard_addaccountbody),
				addedAccountListener, root.findViewById(R.id.add_accounts_btn));

	}

	public void showcaseRefreshAccountStage() {
		closeBalance(getSwipeLayout());
		startShowcase(activity.getString(R.string.onboard_refreshhead), activity.getString(R.string.onboard_refreshbody),
				refreshShowcaseClickListener, root.findViewById(R.id.homeTimeAgo));
	}

	public void showcasePeekBalanceStage() {
		openBalance(getSwipeLayout());
		startShowcase(activity.getString(R.string.onboard_peekhead), activity.getString(R.string.onboard_peekbody),
				peekBalanceShowcaseClickListener, ((RecyclerView) root.findViewById(R.id.balances_recyclerView)).getChildAt(0).findViewById(R.id.balance_drag));
	}

	private SwipeRevealLayout getSwipeLayout() {
		if (root != null && root.findViewById(R.id.balances_recyclerView) != null &&
			    ((RecyclerView) root.findViewById(R.id.balances_recyclerView)).getChildCount() > 0 &&
			    ((RecyclerView) root.findViewById(R.id.balances_recyclerView)).getChildAt(0).findViewById(R.id.swipe_reveal_layout) != null)
			return ((SwipeRevealLayout) ((RecyclerView) root.findViewById(R.id.balances_recyclerView)).getChildAt(0).findViewById(R.id.swipe_reveal_layout));
		return null;
	}

	private void openBalance(SwipeRevealLayout v) { if (v != null) v.open(true); }
	private void closeBalance(SwipeRevealLayout v) { if (v != null) v.close(true); }

	public static void maybeSetStageForRefresh(Context c) {
		if(isFullShowcaseNotCompleted(c)) com.hover.sdk.utils.Utils.saveInt(SHOWCASE_STAGE, 1, c);
	}
	public static void maybeSetStageForPeek(Context c) {
		if(isFullShowcaseNotCompleted(c)) com.hover.sdk.utils.Utils.saveInt(SHOWCASE_STAGE, 2, c);
	}
	private static boolean isFullShowcaseNotCompleted(Context context) {
		return (com.hover.sdk.utils.Utils.getSharedPrefs(context).getInt(SHOWCASE_STAGE, 0) <= 2);
	}
	private void setShowcaseCompleted() {
		Utils.saveInt(SHOWCASE_STAGE, 3, activity.getBaseContext());
	}

	BubbleShowCaseListener peekBalanceShowcaseClickListener = new BubbleShowCaseListener() {

		@Override public void onBubbleClick(@NotNull BubbleShowCase bubbleShowCase) {}

		@Override
		public void onBackgroundDimClick(@NotNull BubbleShowCase bubbleShowCase) {
			bubbleShowCase.dismiss();
			setShowcaseCompleted();
		}

		@Override
		public void onCloseActionImageClick(@NotNull BubbleShowCase bubbleShowCase) {
			bubbleShowCase.dismiss();
			setShowcaseCompleted();
		}

		@Override
		public void onTargetClick(@NotNull BubbleShowCase bubbleShowCase) {
			setShowcaseCompleted();
		}
	};

	BubbleShowCaseListener refreshShowcaseClickListener = new BubbleShowCaseListener() {

		@Override public void onBubbleClick(@NotNull BubbleShowCase bubbleShowCase) {}

		@Override
		public void onBackgroundDimClick(@NotNull BubbleShowCase bubbleShowCase) { bubbleShowCase.dismiss(); }

		@Override
		public void onCloseActionImageClick(@NotNull BubbleShowCase bubbleShowCase) { bubbleShowCase.dismiss(); }

		@Override
		public void onTargetClick(@NotNull BubbleShowCase bubbleShowCase) {
		}
	};

	BubbleShowCaseListener addedAccountListener = new BubbleShowCaseListener() {

		@Override public void onBubbleClick(@NotNull BubbleShowCase bubbleShowCase) {}

		@Override
		public void onBackgroundDimClick(@NotNull BubbleShowCase bubbleShowCase) { bubbleShowCase.dismiss(); }

		@Override
		public void onCloseActionImageClick(@NotNull BubbleShowCase bubbleShowCase) { bubbleShowCase.dismiss(); }

		@Override
		public void onTargetClick(@NotNull BubbleShowCase bubbleShowCase) {
			bubbleShowCase.dismiss();
			goToAddAccountActivity();
		}
	};

	private void goToAddAccountActivity() {
		Amplitude.getInstance().logEvent(activity.getString(R.string.click_add_account));
		activity.startActivityForResult(new Intent(activity, ChannelsActivity.class), Constants.ADD_SERVICE);
	}

	/*
	public void startFullShowcase() {
		startShowcase(activity.getString(R.string.onboard_sechead), activity.getString(R.string.onboard_secbody),
				stagedBubbleListener, root.findViewById(R.id.home_stax_logo));
	}
	private void showcaseThirdStage() {
		openBalance(getSwipeLayout());
		startShowcase(activity.getString(R.string.onboard_balhead), activity.getString(R.string.onboard_balbody),
				stagedBubbleListener, ((RecyclerView) root.findViewById(R.id.balances_recyclerView)).getChildAt(0).findViewById(R.id.balance_drag));
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
			closeBalance(getSwipeLayout());
			Utils.saveInt(ShowcaseExecutor.SHOW_TUTORIAL, 1, activity);
			bubbleShowCase.finishSequence();
		}

		@Override
		public void onBubbleClick(@NotNull BubbleShowCase bubbleShowCase) {
			showcaseNextStage(bubbleShowCase);
		}
	}; */
}
