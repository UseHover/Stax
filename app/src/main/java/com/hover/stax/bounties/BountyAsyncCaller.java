package com.hover.stax.bounties;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hover.sdk.utils.Utils;
import com.hover.sdk.utils.VolleySingleton;
import com.hover.stax.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class BountyAsyncCaller extends AsyncTask<String, Void, Integer> {
    private static final String TAG = "BountyAsyncCaller";

    private WeakReference<Context> context;
    private AsyncResponseListener responseListener;
    private final OkHttpClient client = new OkHttpClient();

    public BountyAsyncCaller(@NonNull WeakReference<Context> mWeakContext, AsyncResponseListener listener) {
        this.context = mWeakContext;
        this.responseListener = listener;
    }

    public interface AsyncResponseListener {
        void onComplete(Integer responseCode);
    }

    @Override
    protected Integer doInBackground(String... params) {
        return uploadBountyUser(getUrl(), getJson(params[0]));
    }

    private String getUrl() {
        return context.get().getString(R.string.api_url) + context.get().getString(R.string.bounty_endpoint);
    }

    private JSONObject getJson(String email) {
        JSONObject root = new JSONObject();
        try {
            JSONObject stax_bounty_hunter = new JSONObject();
            stax_bounty_hunter.put("email", email);
            stax_bounty_hunter.put("device_id", Utils.getDeviceId(context.get()));
            root.put("stax_bounty_hunter", stax_bounty_hunter);
            Log.d(TAG, "uploading " + root);
        } catch (JSONException e) { }
        return root;
    }

    private Integer uploadBountyUser(String url, JSONObject json) {
        try {
            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder().url(url)
                .addHeader("Authorization", "Token token=" + VolleySingleton.getInstance(context.get()).getApiKey())
                .post(body)
                .build();
            Response response = client.newCall(request).execute();
            Log.v(TAG, response.toString());
            return response.code();
        } catch (IOException e) { return 0; }
    }

    @Override
    protected void onPostExecute(Integer responseCode) {
        super.onPostExecute(responseCode);
        if (responseCode != 0)
            responseListener.onComplete(responseCode);
    }
}
