package com.hover.stax.requests;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.util.List;

class SmsSentObserver extends ContentObserver {
	private static final Uri uri = Uri.parse("content://sms/");

	private static final int MESSAGE_TYPE_SENT = 2;
	private static final String COLUMN_ADDRESS = "address";
	private static final String COLUMN_TYPE = "type";
	private static final String[] PROJECTION = { COLUMN_ADDRESS, COLUMN_TYPE };

	private SmsSentListener listener;
	private ContentResolver resolver;
	private Handler handler;

	private List<String> phoneNumbers;
	private long timeout = 30;
	private boolean wasSent = false;
	private boolean timedOut = false;

	public SmsSentObserver(SmsSentListener l, List<String> numbers, Handler handler, Context c) {
		super(handler);

		this.listener = l;
		this.resolver = c.getContentResolver();
		this.phoneNumbers = numbers;
		this.handler = handler;
	}

	private Runnable runOut = new Runnable() {
		@Override
		public void run() {
			if (!wasSent) {
				timedOut = true;
				callBack();
			}
		}
	};

	public void start() {
		resolver.registerContentObserver(uri, true, this);
		handler.postDelayed(runOut, timeout);
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
		Log.e("OBSERVE", "detected change");
		if (wasSent || timedOut)
			return;

		Cursor cursor = null;

		try {
			cursor = resolver.query(uri, PROJECTION, null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				final String address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS));
				final int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
				Log.e("OBSERVE", "address: " + address);
				Log.e("OBSERVE", "type: " + type);
				for (String number: phoneNumbers) {
					if (PhoneNumberUtils.compare(address, number) && type == MESSAGE_TYPE_SENT) {
						wasSent = true;
						callBack();
						break;
					}
				}
			}
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	public interface SmsSentListener {
		void onSmsSendEvent(boolean sent);
	}
}
