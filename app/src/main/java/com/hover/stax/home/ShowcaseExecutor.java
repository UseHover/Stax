package com.hover.stax.home;

import android.app.Activity;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
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

	public void startShowcasing() {
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

	private void showcaseSecondStage() {
		BubbleShowCase.Companion.showCase(
				activity.getString(R.string.onboard_peekhead),
				activity.getString(R.string.onboard_peekbody),
				BubbleShowCase.ArrowPosition.TOP,
				stagedBubbleListener,
				((RecyclerView) root.findViewById(R.id.balances_recyclerView)).getChildAt(0).findViewById(R.id.balance_drag),
				activity);
	}

	private void showcaseThirdStage() {
		BubbleShowCase.Companion.showCase(
				activity.getString(R.string.onboard_balhead),
				activity.getString(R.string.onboard_balbody),
				BubbleShowCase.ArrowPosition.TOP,
				stagedBubbleListener,
				root.findViewById(R.id.homeTimeAgo),
				activity);
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
}
