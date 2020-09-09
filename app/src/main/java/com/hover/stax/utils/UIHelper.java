package com.hover.stax.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.models.StaxContactModel;

import java.util.ArrayList;

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

	public static void loadSpinnerItems(ArrayList<String> entries, AppCompatSpinner spinner, Context context) {
		ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_items, entries);
		spinner.setAdapter(adapter);
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

	public static StaxContactModel getContactInfo(Intent data, View view) {
		Uri contactData = data.getData();
		if (contactData != null) {
			Cursor cur = ApplicationInstance.getContext().getContentResolver().query(contactData, null, null, null, null);
			if (cur != null) {
				if (cur.getCount() > 0) {
					if (cur.moveToNext()) {
						String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
						String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

						if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

							Cursor phones = ApplicationInstance.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
							if (phones != null) {
								StaxContactModel staxContactModel = new StaxContactModel();
								while (phones.moveToNext()) {
									String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

									staxContactModel.setName(name);
									staxContactModel.setPhoneNumber(phoneNumber);

								}
								phones.close();
								return staxContactModel;
							} else return null;// ShowError

						}

					}
				}
				cur.close();
			} else return null;///error

		} else return null;

		return null;
	}

}
