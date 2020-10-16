package com.hover.stax.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

public class UIHelper {

	private static final int INITIAL_ITEMS_FETCH = 30;

	public static void flashMessage(Context context, @Nullable View view, String message) {
		if (view == null) flashMessage(context, message);
		else Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
	}

	public static void flashMessage(@NotNull Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static LinearLayoutManager setMainLinearManagers(Context context) {
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
		linearLayoutManager.setInitialPrefetchItemCount(INITIAL_ITEMS_FETCH);
		linearLayoutManager.setSmoothScrollbarEnabled(true);
		return linearLayoutManager;
	}

	static public void setColoredDrawable(ImageButton imageButton, int drawable, int color) {
		Drawable unwrappedDrawable = AppCompatResources.getDrawable(imageButton.getContext(), drawable);
		assert unwrappedDrawable != null;
		Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
		DrawableCompat.setTint(wrappedDrawable, color);
		imageButton.setImageDrawable(wrappedDrawable);
	}

	public static void fixListViewHeight(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	public static int dpToPx(int dp) {
		return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
	}
}
