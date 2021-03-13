package com.hover.stax.bounties;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hover.sdk.api.HoverParameters;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.navigation.AbstractNavigationActivity;
import com.hover.stax.utils.Constants;
import com.hover.stax.views.StaxDialog;

public class BountyActivity extends AbstractNavigationActivity {
	private static final String TAG = "BountyActivity";
	private static final int BOUNTY_REQUEST = 3000;
	public BountyViewModel bountyViewModel;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bountyViewModel = new ViewModelProvider(this).get(BountyViewModel.class);
		setContentView(R.layout.activity_bounty);
		setUpNav();
	}

	private void makeCall(Action a) {
		Intent i = new HoverParameters.Builder(this).request(a.public_id).setEnvironment(HoverParameters.MANUAL_ENV).buildIntent();
		startActivityForResult(i, BOUNTY_REQUEST);
	}

	public void runAction(Action a) {
		new StaxDialog(this)
				.setDialogTitle(getString(R.string.bounty_claim_title, a.root_code, a.getHumanFriendlyType(this), a.bounty_amount))
				.setDialogMessage(getString(R.string.bounty_claim_explained, a.bounty_amount, a.getInstructions(this)))
				.setPosButton(R.string.start_USSD_Flow, v -> {
					makeCall(a);
				})
				.showIt();
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
