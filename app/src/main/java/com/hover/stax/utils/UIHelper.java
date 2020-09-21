package com.hover.stax.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.transactions.TransactionDescriptionClickListener;
import com.hover.stax.transactions.ClickType;

import org.jetbrains.annotations.NotNull;

public class UIHelper {

	private static final int INITIAL_ITEMS_FETCH = 30;

	public static void flashMessage(Context context, @Nullable View view, String message) {
		if (view == null) flashMessage(context, message);
		else Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
	}

	public static void flashMessage(Context context, String message) {
		if (context == null) context = ApplicationInstance.getContext();
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static LinearLayoutManager setMainLinearManagers(Context context) {
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
		linearLayoutManager.setInitialPrefetchItemCount(INITIAL_ITEMS_FETCH);
		linearLayoutManager.setSmoothScrollbarEnabled(true);
		return linearLayoutManager;
	}

	static public void setTextColoredDrawable(TextView textView, int drawable, int color) {
		Drawable unwrappedDrawable = AppCompatResources.getDrawable(ApplicationInstance.getContext(), drawable);
		assert unwrappedDrawable != null;
		Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
		DrawableCompat.setTint(wrappedDrawable, color);
		textView.setCompoundDrawablesWithIntrinsicBounds(wrappedDrawable, null, null, null);
	}

	public static void changeStatusBarColor(final Activity activity, final int color) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
			return;

		final Window window = activity.getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
		window.setStatusBarColor(color);
	}
	public static void makeChannelNameALink(final String text, final TextView tv, int posStart, int posEnd, TransactionDescriptionClickListener clickListener) {
		if (text == null || tv == null) { return; }
		final SpannableString ss = new SpannableString(text);
		final String[] items = text.split(" ");
		StringBuilder initialNonStart = new StringBuilder();
		for(int j = 0; j< posStart; j++) {
			initialNonStart.append(items[j]).append(" ");
		}

		int start = initialNonStart.length(); //adding plus 1 because of spacing
		int end;
		for (int i = 0; i<items.length; i++) {
			String item = items[i];
			end = start + item.length();
			if(i>=posStart && i<=posEnd) {

				if (start < end) {
					ss.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),start,end, 0 );
					ss.setSpan(new MyClickableSpan(ClickType.CHANNEL, clickListener), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					ss.setSpan(new ForegroundColorSpan(Color.WHITE), start,end, 0);
				}
			}
			else {
				try {
					ss.setSpan(new MyClickableSpan(ClickType.TRANSACTION, clickListener), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}catch (Exception ignored){};
			}
			start += item.length()+1;//comma and space in the original text ;)


		}
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(ss, TextView.BufferType.SPANNABLE);
	}

	private static class MyClickableSpan extends ClickableSpan {
		private final TransactionDescriptionClickListener clickListener;
		private final ClickType clickType;

		private MyClickableSpan(ClickType clickType, TransactionDescriptionClickListener clickListener) {
			this.clickListener = clickListener;
			this.clickType = clickType;
		}

		@Override
		public void updateDrawState(@NonNull TextPaint ds) {
			super.updateDrawState(ds);
			ds.setUnderlineText(false);
		}

		@Override
		public void onClick(@NotNull final View widget) {
			clickListener.onClickChannel(clickType);
		}

	}
}
