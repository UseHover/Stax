package com.hover.stax.hover;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.sdk.api.HoverParameters;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.contacts.PhoneHelper;
import com.hover.stax.settings.KeyStoreExecutor;
import com.hover.stax.utils.Constants;
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
        Hover.setPermissionActivity(Constants.PERM_ACTIVITY, b.activity);
        frag = b.fragment;
        channel = b.channel;
        requestCode = b.requestCode;
        finalScreenTime = b.finalScreenTime;
        HoverParameters.Builder builder = getBasicBuilder(b);
        addExtras(builder, b.extras, b.action);
//		addPin(builder, b.activity); // Not active
        startHover(builder, b.activity);
    }

    private HoverParameters.Builder getBasicBuilder(Builder b) {
        HoverParameters.Builder builder = new HoverParameters.Builder(b.activity);
        builder.setEnvironment(Utils.getBoolean(Constants.TEST_MODE, b.activity) ? HoverParameters.TEST_ENV : HoverParameters.PROD_ENV);
        builder.request(b.action.public_id);
        builder.setHeader(getMessage(b.action, b.activity));
        builder.initialProcessingMessage("");
        builder.showUserStepDescriptions(true);
        builder.finalMsgDisplayTime(finalScreenTime);
        builder.style(R.style.StaxHoverTheme);
        builder.sessionOverlayLayout(R.layout.stax_transacting_in_progress);
        return builder;
    }

    private void addExtras(HoverParameters.Builder builder, JSONObject extras, HoverAction action) {
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
        if (key.equals(HoverAction.PHONE_KEY)) {
            return PhoneHelper.normalizeNumberByCountry(value, channel.countryAlpha2);
        }
        return value;
    }

    private void addPin(HoverParameters.Builder builder, Activity a) {
        builder.extra(HoverAction.PIN_KEY, KeyStoreExecutor.decrypt(channel.pin, a));
    }

    private String getMessage(HoverAction a, Context c) {
        switch (a.transaction_type) {
            case HoverAction.BALANCE:
                return c.getString(R.string.balance_msg, a.from_institution_name);
            case HoverAction.AIRTIME:
                return c.getString(R.string.airtime_msg);
            default:
                return c.getString(R.string.transfer_msg);
        }
    }

    private void startHover(HoverParameters.Builder builder, Activity a) {
        Intent i = builder.buildIntent();
        Utils.logAnalyticsEvent(a.getString(R.string.start_load_screen), a);
        if (frag != null)
            frag.startActivityForResult(i, requestCode);
        else
            a.startActivityForResult(i, requestCode);
    }

    public static class Builder {
        private final Activity activity;
        private Fragment fragment;
        private Channel channel;
        private HoverAction action;
        private JSONObject extras;
        private int requestCode, finalScreenTime = 4000;

        public Builder(HoverAction a, Channel c, Activity activity, int code) {
            if (a == null) throw new IllegalArgumentException("Action must not be null");
            this.activity = activity;
            channel = c;
            action = a;
            extras = new JSONObject();
            requestCode = code;
        }

        public Builder(HoverAction a, Channel c, Activity act, int requestCode, Fragment frag) {
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
