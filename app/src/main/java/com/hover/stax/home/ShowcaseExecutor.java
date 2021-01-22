package com.hover.stax.home;

import android.app.Activity;
import android.content.Context;
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
import com.hover.stax.utils.customSwipeRefresh.CustomSwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import static com.hover.stax.database.Constants.SHOWCASE_STAGE;

public class ShowcaseExecutor {
	private static String TAG = "ShowcaseExecutor";
	public static String SHOW_TUTORIAL = "SHOWCASE";
	private Activity activity;
	private View root;
	private int stage = 0;

	private boolean isShowing;

	public ShowcaseExecutor(Activity a, View view) {
		activity = a;
		root = view;
		isShowing = false;
	}

	public static int getStage(Context c) { return Utils.getSharedPrefs(c).getInt(Constants.SHOWCASE_STAGE, 0); }
	public static void increaseStage(Context c) { Utils.saveInt(Constants.SHOWCASE_STAGE, getStage(c) + 1, c); }

	private void startShowcase(String head, String body, BubbleShowCaseListener listener, View view) {
		try {
			if (!isShowing)
				BubbleShowCase.Companion.showCase(head, body, BubbleShowCase.ArrowPosition.TOP, listener, view, activity);
			isShowing = true;
		} catch (Exception e) { Log.e(TAG, "Showcase failed to start", e); }
	}

	public void showcaseAddAcctStage() {
		startShowcase(activity.getString(R.string.onboard_addaccounthead), activity.getString(R.string.onboard_addaccountbody),
				addedAccountListener, root.findViewById(R.id.add_accounts_btn));
	}

	public void showcaseRefreshAccountStage() {
		CustomSwipeRefreshLayout csrl = ((CustomSwipeRefreshLayout) root.findViewById(R.id.swipelayout));
		if (csrl != null)
			csrl.animateOffsetToTriggerPosition(0, null);
		startShowcase(activity.getString(R.string.onboard_refreshhead), activity.getString(R.string.onboard_refreshbody),
				refreshShowcaseClickListener, root.findViewById(R.id.homeTimeAgo));
	}

	public void showcasePeekBalanceStage() {

	}

	private void openBalance(SwipeRevealLayout v) { if (v != null) v.open(true); }
	private void closeBalance(SwipeRevealLayout v) { if (v != null) v.close(true); }

	BubbleShowCaseListener addedAccountListener = new BubbleShowCaseListener() {

		@Override public void onBubbleClick(@NotNull BubbleShowCase bubbleShowCase) {}

		@Override
		public void onBackgroundDimClick(@NotNull BubbleShowCase bubbleShowCase) { endStage(bubbleShowCase); }

		@Override
		public void onCloseActionImageClick(@NotNull BubbleShowCase bubbleShowCase) { endStage(bubbleShowCase); }

		@Override
		public void onTargetClick(@NotNull BubbleShowCase bubbleShowCase) {
			endStage(bubbleShowCase);
			goToAddAccountActivity();
		}
	};

	BubbleShowCaseListener refreshShowcaseClickListener = new BubbleShowCaseListener() {

		@Override public void onBubbleClick(@NotNull BubbleShowCase bubbleShowCase) {}

		@Override
		public void onBackgroundDimClick(@NotNull BubbleShowCase bubbleShowCase) {
			endStage(bubbleShowCase);
			((CustomSwipeRefreshLayout) root.findViewById(R.id.swipelayout)).animateStayComplete(null);
		}

		@Override
		public void onCloseActionImageClick(@NotNull BubbleShowCase bubbleShowCase) {
			endStage(bubbleShowCase);
			((CustomSwipeRefreshLayout) root.findViewById(R.id.swipelayout)).animateStayComplete(null);
		}

		@Override
		public void onTargetClick(@NotNull BubbleShowCase bubbleShowCase) {}
	};

	BubbleShowCaseListener peekBalanceShowcaseClickListener = new BubbleShowCaseListener() {

		@Override public void onBubbleClick(@NotNull BubbleShowCase bubbleShowCase) {}

		@Override
		public void onBackgroundDimClick(@NotNull BubbleShowCase bubbleShowCase) {
			endStage(bubbleShowCase);
		}

		@Override
		public void onCloseActionImageClick(@NotNull BubbleShowCase bubbleShowCase) {
			endStage(bubbleShowCase);
		}

		@Override
		public void onTargetClick(@NotNull BubbleShowCase bubbleShowCase) {
			endStage(bubbleShowCase);
		}
	};

	private void goToAddAccountActivity() {
		Amplitude.getInstance().logEvent(activity.getString(R.string.click_add_account));
		activity.startActivityForResult(new Intent(activity, ChannelsActivity.class), Constants.ADD_SERVICE);
	}

	private void endStage(BubbleShowCase bubbleShowCase) {
		increaseStage(activity);
		bubbleShowCase.dismiss();
		isShowing = false;
	}
}
