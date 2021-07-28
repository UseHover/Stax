package com.hover.stax.bounties;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.hover.sdk.api.Hover;
import com.hover.stax.R;
import com.hover.stax.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

class BountyAsyncCaller extends AsyncTask<String, Void, Map<Integer, String>> {
    private static final String TAG = "BountyAsyncCaller";

    private final WeakReference<Context> context;
    private final AsyncResponseListener responseListener;
    private final OkHttpClient client = new OkHttpClient();

    public BountyAsyncCaller(@NonNull WeakReference<Context> mWeakContext, AsyncResponseListener listener) {
        this.context = mWeakContext;
        this.responseListener = listener;
    }

    public interface AsyncResponseListener {
        void onComplete(Map<Integer, String>  responseMap);
    }

    @Override
    protected Map<Integer, String> doInBackground(String... params) {
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
            stax_bounty_hunter.put("device_id", Hover.getDeviceId(context.get()));
            root.put("stax_bounty_hunter", stax_bounty_hunter);

            Timber.d( "uploading %s", root);
        } catch (JSONException ignored) {
        }
        return root;
    }

    private Map<Integer, String> uploadBountyUser(String url, JSONObject json) {
        Map<Integer, String> resultMap = new HashMap<>();
        try {
            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder().url(url)
                    .addHeader("Authorization", "Token token=" + Hover.getApiKey(context.get()))
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            resultMap.put(response.code(), response.toString());
            return resultMap;
        } catch (IOException e) {
            resultMap.put(0, e.getMessage());
            return resultMap;
        }
    }

    @Override
    protected void onPostExecute(Map<Integer, String> responseMap) {
        super.onPostExecute(responseMap);
        responseListener.onComplete(responseMap);
    }
}
