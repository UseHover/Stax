package com.hover.stax.channels;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.hover.stax.R;
import com.hover.stax.database.AppDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateChannelsWorker extends Worker {
	public final static String TAG = "UpdateChannelsWorker";

	public static String CHANNELS_WORK_ID = "CHANNELS";
	private final OkHttpClient client = new OkHttpClient();
	private final ChannelDao channelDao;
	private String errorMsg = null;

	public UpdateChannelsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
		super(context, params);
		channelDao = AppDatabase.getInstance(context).channelDao();
	}

	public static PeriodicWorkRequest makeToil() {
		return new PeriodicWorkRequest.Builder(UpdateChannelsWorker.class, 24, TimeUnit.HOURS)
			       .setConstraints(netConstraint())
			       .build();
	}

	public static OneTimeWorkRequest makeWork() {
		return new OneTimeWorkRequest.Builder(UpdateChannelsWorker.class)
			       .setConstraints(netConstraint())
			       .build();
	}

	public static Constraints netConstraint() {
		return new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
	}

	@Override
	public Worker.Result doWork() {
		try {
			Log.v(TAG, "Downloading channels...");
			JSONObject channelsJson = downloadChannels(getUrl());
			JSONArray data = channelsJson.getJSONArray("data");
			for (int j = 0; j < data.length(); j++) {
				channelDao.insert(new Channel(data.getJSONObject(j).getJSONObject("attributes")));
			}
			Log.i(TAG, "Successfully downloaded and saved channels.");
			return Result.success();
		} catch (JSONException | NullPointerException e) {
			Log.e(TAG, "Error parsing channel data.", e);
			return Result.failure();
		} catch (IOException e) {
			Log.e(TAG, "Timeout downloading channel data, will try again.", e);
			return Result.retry();
		}
	}

	private String getUrl() {
		return getApplicationContext().getString(R.string.root_url) + getApplicationContext().getString(R.string.channels_endpoint);
	}

	private JSONObject downloadChannels(String url) throws IOException, JSONException {
		Request request = new Request.Builder().url(url).build();
		Response response = client.newCall(request).execute();
		JSONObject data = new JSONObject(response.body().string());
		return data;
//		return data.getJSONArray("attributes");
	}
}
