package com.hover.stax.hover;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.amplitude.api.Amplitude;
import com.hover.sdk.api.Hover;
import com.hover.sdk.api.HoverParameters;
//import com.hover.sdk.api.HoverTemplates;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.Constants;
import com.hover.stax.security.KeyStoreExecutor;
import com.hover.stax.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

final public class HoverSession {
	private final static String TAG = "HoverSession";

	final private Fragment frag;
	final private Channel channel;
	final private int requestCode, finalScreenTime;


	private HoverSession(Builder b) {
		Hover.setAfterPermissionReturnActivity(Hover.DEFAULT_PERM_ACTIVITY, b.activity);
		frag = b.fragment;
		channel = b.channel;
		requestCode = b.requestCode;
		finalScreenTime = b.finalScreenTime;
		HoverParameters.Builder builder = getBasicBuilder(b);
		addExtras(builder, b.extras, b.action);
		addPin(builder, b.activity);
		startHover(builder, b.activity);
	}

	private HoverParameters.Builder getBasicBuilder(Builder b) {
		HoverParameters.Builder builder = new HoverParameters.Builder(b.activity);
		builder.request(b.action.public_id);
		builder.setEnvironment(HoverParameters.PROD_ENV);
		builder.initialProcessingMessage(getMessage(b.action, b.activity));
		builder.showUserStepDescriptions(true);
		builder.finalMsgDisplayTime(finalScreenTime);
		builder.style(R.style.StaxHoverTheme);
		//builder.sty(Constants.STYLE_MODE_FOR_STAX);
		builder.transactingImages(getSenderLogo(), getReceiverLogo(b.action));
		builder.customBackgroundImage(R.drawable.stax_background);
		builder.styleMode(R.layout.stax_transacting_in_progress);

		return builder;
	}

	private void addExtras(HoverParameters.Builder builder, JSONObject extras, Action action) {
		List<String> required_extras = action.getRequiredParams();
		Iterator<?> keys = extras.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			String normalizedVal = parseExtra(key, extras.optString(key), required_extras);
			if (normalizedVal != null)
				builder.extra(key, normalizedVal);
		}
	}

	private String parseExtra(String key, String value, List<String> requiredExtras) {
		if (value == null || !requiredExtras.contains(key)) {
			return null;
		}
		if (key.equals(Action.PHONE_KEY)) {
			return StaxContact.normalizeNumberByCountry(value, channel.countryAlpha2);
		}
		return value;
	}

	private void addPin(HoverParameters.Builder builder, Activity a) {
		builder.extra(Action.PIN_KEY, KeyStoreExecutor.decrypt(channel.pin, a));
	}

	private byte[] getSenderLogo() {
		byte[] l = getLogo(channel.logoUrl);
//		Log.e(TAG, "logo array: " + l);
		return l;
	}
	private byte[] getReceiverLogo(Action a) {
		if (a.to_institution_logo != null && !channel.logoUrl.equals(a.to_institution_logo))
			return getLogo(a.to_institution_logo);
		return null;
	}
	private byte[] getLogo(String url) {
//		try {
//			Log.e(TAG, "logo url: " + url);
//			Bitmap b = Picasso.get().load(url).networkPolicy(NetworkPolicy.OFFLINE).get();
//			Log.e(TAG, "bitmap: " + b);
//			return Utils.bitmapToByteArray(b);
//		} catch (Exception ignored) {
//			Log.e(TAG, "exception", ignored);
			return null;
//		}
	}

	private String getMessage(Action a, Context c) {
		switch (a.transaction_type) {
			case Action.BALANCE: return c.getString(R.string.balance_msg, a.from_institution_name);
			case Action.AIRTIME: return c.getString(R.string.airtime_msg);
			default: return c.getString(R.string.transfer_msg);
		}
	}

	private void startHover(HoverParameters.Builder builder, Activity a) {
		Intent i = builder.buildIntent();
		Amplitude.getInstance().logEvent(a.getString(R.string.start_load_screen));
		if (frag != null)
			frag.startActivityForResult(i, requestCode);
		else
			a.startActivityForResult(i, requestCode);
	}

	public static class Builder {
		private final Activity activity;
		private Fragment fragment;
		private Channel channel;
		private Action action;
		private JSONObject extras;
		private int requestCode, finalScreenTime = 2000;

		public Builder(Action a, Channel c, Activity act, int code) {
			if (a == null) throw new IllegalArgumentException("Action must not be null");
			activity = act;
			channel = c;
			action = a;
			extras = new JSONObject();
			requestCode = code;
		}

		public Builder(Action a, Channel c, Activity act, int requestCode, Fragment frag) {
			this(a, c, act, requestCode);
			fragment = frag;
		}

		public HoverSession.Builder extra(String key, String value) {
			try {
				extras.put(key, value);
			} catch (JSONException e) {
				Log.e(TAG, "Failed to add extra");
			}
			return this;
		}

		public HoverSession.Builder finalScreenTime(int ms) {
			finalScreenTime = ms;
			return this;
		}

		public HoverSession run() {
			return new HoverSession(this);
		}
	}
}
