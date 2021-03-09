package com.hover.stax.bounty;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;
import com.hover.sdk.utils.AnalyticsSingleton;
import com.hover.stax.R;
import com.hover.stax.database.AppDatabase;
import com.hover.stax.utils.StaxVolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdateBountyWorker extends Worker {
	public final static String TAG = "UpdateBountyWorker";
	public static String BOUNTY_WORK_ID = "BOUNTY";
	private final BountyUserDao bountyUserDao;

	public UpdateBountyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
		bountyUserDao = AppDatabase.getInstance(context).bountyDao();
	}

	public static PeriodicWorkRequest makeToil() {
		return new PeriodicWorkRequest.Builder(UpdateBountyWorker.class, 6, TimeUnit.HOURS)
					   .setConstraints(netConstraint())
					   .build();
	}

	public static OneTimeWorkRequest makeWork() {
		return new OneTimeWorkRequest.Builder(UpdateBountyWorker.class)
					   .setConstraints(netConstraint())
					   .build();
	}

	public static Constraints netConstraint() {
		return new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
	}

	private String getUrl() {
		return getApplicationContext().getString(R.string.api_url) + getApplicationContext().getString(R.string.bounty_endpoint);
	}

	@NonNull
	@Override
	public Result doWork() {
		Log.v(TAG, "Checking for yet to uploaded bounty user...");
		BountyUser bountyUser = bountyUserDao.getUser();
		if (bountyUser != null && !bountyUser.isUploaded) {
			return uploadBountyUser(bountyUser);
		} else {
			Log.i(TAG, "Empty yet to be uploaded bounty user.");
			return Result.success();
		}
	}

	public Result uploadBountyUser(BountyUser bountyUser) {
		try {
			Log.v(TAG, "Uploading: " + bountyUser.toJson().toString());
			JSONObject response = StaxVolleySingleton.uploadNow(getApplicationContext(), Request.Method.POST, getUrl(), bountyUser.toJson());
			Log.v(TAG, response.toString());
			return processResponse(bountyUser, response);
		} catch (NullPointerException | JSONException je) {
			AnalyticsSingleton.capture(getApplicationContext(), je);
			Log.d(TAG, "Failed to process response JSON", je);
			return ListenableWorker.Result.retry();
		} catch (TimeoutException | ExecutionException | InterruptedException e) {
			StaxVolleySingleton.exceptionHandler(getApplicationContext(), e);
			return ListenableWorker.Result.retry();
		}
	}

	private Worker.Result processResponse(BountyUser bountyUser, JSONObject response) {
		Log.v(TAG, "parsing bounty user response: " + response);
		if (response.has("created_or_updated")) {
			bountyUser.uploadedTimestamp = new Date().getTime();
			bountyUser.isUploaded = true;
			bountyUserDao.update(bountyUser);
			return Result.success();
		}
		return Result.retry();
	}
}
