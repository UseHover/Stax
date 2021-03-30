package com.hover.stax.bounties;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.amplitude.api.Amplitude;
import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.HoverParameters;
import com.hover.stax.R;
import com.hover.stax.navigation.AbstractNavigationActivity;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.StaxDialog;

public class BountyActivity extends AbstractNavigationActivity {
	private static final String TAG = "BountyActivity";
	static final String EMAIL_KEY = "email_for_bounties";
	private static final int BOUNTY_REQUEST = 3000;
	public BountyViewModel bountyViewModel;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, TAG));
		bountyViewModel = new ViewModelProvider(this).get(BountyViewModel.class);
		setContentView(R.layout.activity_bounty);
		setUpNav();
		if (!Utils.getString(EMAIL_KEY, this).isEmpty())
			Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.bountyListFragment);
		else
			Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_bounty_email)));
	}

	public void makeCall(HoverAction a) {
		Amplitude.getInstance().logEvent(getString(R.string.clicked_run_bounty_session, a.from_institution_name, a.root_code));
		call(a.public_id);
	}

	public void retryCall(String actionId) {
		Amplitude.getInstance().logEvent(getString(R.string.clicked_retry_bounty_session));
		call(actionId);
	}
	private void call(String actionId) {
		Intent i = new HoverParameters.Builder(this).request(actionId).setEnvironment(HoverParameters.MANUAL_ENV).buildIntent();
		startActivityForResult(i, BOUNTY_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == BOUNTY_REQUEST && resultCode == RESULT_OK) {
			new StaxDialog(this)
					.setDialogTitle(R.string.flow_recorded)
					.setDialogMessage(R.string.bounty_flow_pending_dialog_msg)
					.setPosButton(R.string.go_through_another_flow, null)
					.showIt();
		}
	}
}
