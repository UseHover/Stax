package com.hover.stax.bounty;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hover.sdk.utils.AnalyticsSingleton;
import com.hover.stax.R;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class BountyAsyncCaller extends AsyncTask<Void, Void, String> {
    private static final String TAG = "BountyAsyncCaller";
    private WeakReference<Context> mWeakContext;
    private BountyViewModel viewModel;
    public BountyAsyncCaller(@NonNull WeakReference<Context> mWeakContext, @NonNull BountyViewModel viewModel) {
        this.mWeakContext = mWeakContext;
        this.viewModel = viewModel;
    }

    @Override
    protected String doInBackground(Void... params) {
        String email = viewModel.getEmail();
        String deviceId = com.hover.sdk.utils.Utils.getDeviceId(mWeakContext.get());
        Log.d(TAG,  "email: "+email + "device id: "+deviceId);
        return uploadBountyUser(email, deviceId);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        viewModel.setUploadBountyUserResultLiveData(s);
    }


    private String getUrl() {
        return mWeakContext.get().getString(R.string.api_url) + mWeakContext.get().getString(R.string.bounty_endpoint);
    }
    private String uploadBountyUser(String email, String deviceId) {
        try {
            Log.v(TAG, "Uploading: " + toJson(email, deviceId));
            return  upload(toJson(email, deviceId).toString());
        } catch (NullPointerException | JSONException je) {
            AnalyticsSingleton.capture(mWeakContext.get(), je);
            Log.d(TAG, "Failed to process response JSON", je);
            return mWeakContext.get().getString(R.string.bounty_api_json_error);
        }  catch (IOException e) {
            AnalyticsSingleton.capture(mWeakContext.get(), e);
            Log.d(TAG, "IOException error", e);
            return e.getMessage();
        }
    }

    private String upload(String jsonString) throws IOException {
        final RequestBody body = RequestBody.create(jsonString, MediaType.parse("application/json"));
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(getUrl())
                                  .addHeader("Authorization", "Token token=" + Utils.getApiKey(mWeakContext.get()))
                                  .post(body)
                                  .build();
        Response response = client.newCall(request).execute();
        return processResponse(response);
    }

    private String processResponse(Response response) {
        Log.v(TAG, "parsing bounty user response: " + response.body());
        Log.v(TAG, "parsing bounty user response code: " + response.code());
        if (response.isSuccessful()) {
            Utils.saveBoolean(Constants.BOUNTY_EMAIL, true, mWeakContext.get());
            return Constants.SUCCESS;
        }
        return mWeakContext.get().getString(R.string.bounty_api_other_error);
    }

    private JSONObject toJson(String email, String deviceId) throws JSONException {
        String format = "{ \"stax_bounty_hunter\": { \"device_id\": " + deviceId + ", \"email\": " + email + "} }";
        return new JSONObject(format);
    }
}
