package com.hover.stax.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.hover.stax.ApplicationInstance;

public class UIHelper {

private static final int INITIAL_ITEMS_FETCH = 30;
public static void flashMessage(Context context, @Nullable View view, String message) {
	if (view == null) flashMessage(context, message);
	else Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
}
public static void flashMessage(Context context, String message) {
	if(context == null) context = ApplicationInstance.getContext();
	Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
}

public static LinearLayoutManager setMainLinearManagers(Context context) {
	LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
	linearLayoutManager.setInitialPrefetchItemCount(INITIAL_ITEMS_FETCH);
	linearLayoutManager.setSmoothScrollbarEnabled(true);
	return  linearLayoutManager;
}

}
