package com.hover.stax.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.hover.stax.R;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

public class UIHelper {
	private static final String TAG = "UIHelper";
	private static final int INITIAL_ITEMS_FETCH = 30;

	public static void flashMessage(Context context, @Nullable View view, String message) {
		if (view == null) flashMessage(context, message);
		else showSnack(view, message);
	}

	private static void showSnack(View view, String message) {
		Snackbar s = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
		s.setAnchorView(view);
		s.show();
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

	public static int getColor(String hex, boolean isBackground, Context c) {
		try {
			return Color.parseColor(hex);
		} catch (IllegalArgumentException e) {
			return ContextCompat.getColor(c, isBackground ? R.color.offWhite : R.color.brightBlue);
		}
	}

	public static void changeDrawableColor(TextView tv, int color, Context c) {
		for (Drawable d : tv.getCompoundDrawables()) {
			if (d != null)
				d.setColorFilter(new PorterDuffColorFilter(c.getResources().getColor(color), PorterDuff.Mode.SRC_IN));
		}
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
			Log.e(TAG, "item height " + listItem.getHeight());
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	public static int dpToPx(int dp) {
		return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
	}

	public static void picassoLoad(String url, int size, Target target) {
		Picasso.get()
				.load(url)
				.config(Bitmap.Config.RGB_565)
				.networkPolicy(NetworkPolicy.OFFLINE)
				.resize(size,size).into(target);
	}
}
