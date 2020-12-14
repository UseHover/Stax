package com.hover.stax.channels;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;

import com.amplitude.api.Amplitude;
import com.hover.sdk.api.Hover;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.database.Constants;
import com.hover.stax.languages.SelectLanguageActivity;
import com.hover.stax.security.PermissionsFragment;
import com.hover.stax.security.PinsActivity;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.hover.stax.database.Constants.AUTH_CHECK;
import static com.hover.stax.database.Constants.LANGUAGE_CHECK;

public class ChannelsActivity extends AppCompatActivity {
	public final static String TAG = "ChannelsActivity";

	ChannelListViewModel channelViewModel;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WorkManager.getInstance(this).beginUniqueWork(UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.KEEP, UpdateChannelsWorker.makeWork()).enqueue();
		setContentView(R.layout.activity_channels);
		channelViewModel = new ViewModelProvider(this).get(ChannelListViewModel.class);
		channelViewModel.getSelected().observe(this, this:: changeContinueClickAction);

		if (new PermissionHelper(this).hasPhonePerm()) goToChannelSelection();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (new PermissionHelper(this).hasPhonePerm()) goToChannelSelection();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PermissionsFragment.PHONE_REQUEST && PermissionUtils.permissionsGranted(grantResults))
			goToChannelSelection();
	}

	private void goToChannelSelection() {
		Hover.updateSimInfo(this);
		Navigation.findNavController(findViewById(R.id.nav_host_fragment)).navigate(R.id.navigation_channels);
		findViewById(R.id.continue_btn).setVisibility(View.VISIBLE);
	}

	private void changeContinueClickAction(List<Integer> channelIds) {
		if (channelIds.size() > 0) findViewById(R.id.continue_btn).setOnClickListener(view ->logChoicesSaveAction(channelIds));
		else findViewById(R.id.continue_btn).setOnClickListener(view -> UIHelper.flashMessage(ChannelsActivity.this, getString(R.string.toast_error_noselect)));
	}
	private void logChoicesSaveAction(List<Integer> channelIds) {
		JSONObject event = new JSONObject();
		try {
			event.put(getString(R.string.account_select_count_key), channelIds.size());
		} catch (JSONException ignored) {
		}
		Amplitude.getInstance().logEvent(getString(R.string.finished_account_select), event);
		saveAndContinue();
	}

	private void saveAndContinue() {
		channelViewModel.saveSelected();
		Utils.saveInt(AUTH_CHECK, 1, this);
		goToMainActivity();
		//startActivityForResult(new Intent(ChannelsActivity.this, PinsActivity.class), 0);
	}
	private void goToMainActivity() {
		setResult(RESULT_OK, addReturnData(new Intent()));
		finish();
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK)
			setResult(RESULT_OK, addReturnData(new Intent()));
		else
			setResult(RESULT_CANCELED);
		finish();
	}

	private Intent addReturnData(Intent i) {
		if (channelViewModel.getSelected().getValue() != null) {
			Bundle bundle = new Bundle();
			bundle.putIntegerArrayList("selected", new ArrayList<>(channelViewModel.getSelected().getValue()));
			i.putExtra("selected", bundle);
		}
		return i;
	}

	@Override
	public void onBackPressed() {
		cancel(null);
		super.onBackPressed();
	}

	public void cancel(View view) {
		setResult(RESULT_CANCELED);
		finish();
	}
}
