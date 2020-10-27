package com.hover.stax.requests;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;

import java.util.List;

class SmsSentObserver extends ContentObserver {
	private static final String TAG = "SmsSentObserver";
	private static final Uri uri = Uri.parse("content://sms/");

	private static final int MESSAGE_TYPE_SENT = 2;
	private static final String COLUMN_TYPE = "type";
	private static final String COLUMN_ADDRESS = "address";
	private static final String[] PROJECTION = { COLUMN_ADDRESS, COLUMN_TYPE };

	private ContentResolver resolver;
	final private SmsSentListener listener;
	final private List<String> phoneNumbers;
	private boolean wasSent = false;
	final private String successMsg;

	public SmsSentObserver(SmsSentListener l, List<String> numbers, Handler handler, Context c) {
		super(handler);

		this.resolver = c.getContentResolver();
		this.listener = l;
		this.phoneNumbers = numbers;

		successMsg = c.getString(R.string.sms_sent_success);
	}

	public void start() {
		resolver.registerContentObserver(uri, true, this);
	}

	public void stop() {
		resolver.unregisterContentObserver(this);
		resolver = null;
	}

	private void callBack() {
		listener.onSmsSendEvent(wasSent);
		stop();
	}

	@Override
	public void onChange(boolean selfChange) {
		if (wasSent) return;

		Cursor cursor = null;
		try {
			cursor = resolver.query(uri, PROJECTION, null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				final String address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS));
				final int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
				for (String number : phoneNumbers) {
					if (PhoneNumberUtils.compare(address, number) && type == MESSAGE_TYPE_SENT) {
						wasSent = true;
						callBack();
						Amplitude.getInstance().logEvent(successMsg);
						break;
					}
				}
			}
		} catch (Exception e) { Log.e(TAG, "FAILURE", e);
		} finally { if (cursor != null) cursor.close(); }
	}

	public interface SmsSentListener {
		void onSmsSendEvent(boolean sent);
	}
}
