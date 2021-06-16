package com.hover.stax.home;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.Utils;
import com.hover.stax.utils.bubbleshowcase.BubbleShowCase;
import com.hover.stax.utils.bubbleshowcase.BubbleShowCaseListener;

import org.jetbrains.annotations.NotNull;

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

    public static int getStage(Context c) {
        return Utils.getSharedPrefs(c).getInt(Constants.SHOWCASE_STAGE, 0);
    }

    public static void increaseStage(Context c) {
        Utils.saveInt(Constants.SHOWCASE_STAGE, getStage(c) + 1, c);
    }

    private void startShowcase(String head, String body, BubbleShowCaseListener listener, View view) {
        try {
            if (!isShowing)
                BubbleShowCase.Companion.showCase(head, body, BubbleShowCase.ArrowPosition.TOP, listener, view, activity);
            isShowing = true;
        } catch (Exception e) {
            Log.e(TAG, "Showcase failed to start", e);
        }
    }

    public void showcasePeekBalanceStage() {

    }

    private void openBalance(SwipeRevealLayout v) {
        if (v != null) v.open(true);
    }

    private void closeBalance(SwipeRevealLayout v) {
        if (v != null) v.close(true);
    }

    BubbleShowCaseListener addedAccountListener = new BubbleShowCaseListener() {

        @Override
        public void onBubbleClick(@NotNull BubbleShowCase bubbleShowCase) {
        }

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

    BubbleShowCaseListener refreshShowcaseClickListener = new BubbleShowCaseListener() {

        @Override
        public void onBubbleClick(@NotNull BubbleShowCase bubbleShowCase) {
        }

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
        }
    };

    BubbleShowCaseListener peekBalanceShowcaseClickListener = new BubbleShowCaseListener() {

        @Override
        public void onBubbleClick(@NotNull BubbleShowCase bubbleShowCase) {
        }

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

    private void endStage(BubbleShowCase bubbleShowCase) {
        increaseStage(activity);
        bubbleShowCase.dismiss();
        isShowing = false;
    }
}
