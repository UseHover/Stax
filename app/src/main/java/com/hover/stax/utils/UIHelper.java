package com.hover.stax.utils;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;

import java.util.ArrayList;
import java.util.List;

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
		return  linearLayoutManager;
	}

	public static void loadSpinnerItems(ArrayList<String> entries, AppCompatSpinner spinner, Context context) {
		ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, entries);

		adapter.setDropDownViewResource(R.layout.spinner_items);
		spinner.setAdapter(adapter);
	}

	public static void setTextUnderline(TextView textView, String cs) {
		SpannableString content = new SpannableString(cs);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		content.setSpan(android.graphics.Typeface.BOLD, 0, content.length(), 0);
		try{
			textView.setText(content);
		}catch (Exception e) {
			//Avoid error due to threading based on users aggressive clicks.
			//I.e when user types in the search, it waits for 1.5secs to update the textView,
			//During this 1.5sec if user goes away from the screen it can through an error called:
			//CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
			//Therefore, putting this in a try and catch to avoid crashing.
		}

	}
}
