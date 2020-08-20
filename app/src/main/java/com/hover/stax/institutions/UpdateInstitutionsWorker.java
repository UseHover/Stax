package com.hover.stax.institutions;

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

public class UpdateInstitutionsWorker extends Worker {
	public final static String TAG = "UpdateInstsWorker";

	private final OkHttpClient client = new OkHttpClient();
	private final InstitutionDao institutionDao;
	private String errorMsg = null;

	public UpdateInstitutionsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
		super(context, params);
		institutionDao = AppDatabase.getInstance(context).institutionDao();
	}

	public static PeriodicWorkRequest makeToil() {
		return new PeriodicWorkRequest.Builder(UpdateInstitutionsWorker.class, 24, TimeUnit.HOURS)
			       .setConstraints(netConstraint())
			       .build();
	}

	public static OneTimeWorkRequest makeWork() {
		return new OneTimeWorkRequest.Builder(UpdateInstitutionsWorker.class)
			       .setConstraints(netConstraint())
			       .build();
	}

	public static Constraints netConstraint() {
		return new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
	}

	@Override
	public Worker.Result doWork() {
		try {
			Log.v(TAG, "Downloading institutions...");
			JSONObject institutionsJson = downloadInstitutions(getUrl());
			JSONArray data = institutionsJson.getJSONArray("data");
			for (int j = 0; j < data.length(); j++) {
				institutionDao.insert(new Institution(data.getJSONObject(j).getJSONObject("attributes")));
			}
			Log.i(TAG, "Successfully downloaded and saved institutions.");
			return Result.success();
		} catch (JSONException | NullPointerException e) {
			Log.e(TAG, "Error parsing institution data.", e);
			return Result.failure();
		} catch (IOException e) {
			Log.e(TAG, "Timeout downloading institution data, will try again.", e);
			return Result.retry();
		}
	}

	private String getUrl() {
		return getApplicationContext().getString(R.string.root_url) + getApplicationContext().getString(R.string.institutions_endpoint);
	}

	private JSONObject downloadInstitutions(String url) throws IOException, JSONException {
		Request request = new Request.Builder().url(url).build();
		Response response = client.newCall(request).execute();
		JSONObject data = new JSONObject(response.body().string());
		return data;
//		return data.getJSONArray("attributes");
	}
}
