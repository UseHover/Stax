package com.hover.stax.bounty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.navigation.AbstractNavigationActivity;
import com.hover.stax.utils.Constants;
import com.hover.stax.views.StaxDialog;

public class BountyActivity extends AbstractNavigationActivity implements BountyRunInterface {
	private static final String TAG = "BountyActivity";
	public BountyViewModel bountyViewModel;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bountyViewModel = new ViewModelProvider(this).get(BountyViewModel.class);
		setContentView(R.layout.activity_bounty);
		setUpNav();
	}

	private void makeCall(Action a) {
		HoverSession.Builder hsb = new HoverSession.Builder(a, null,
				BountyActivity.this, Constants.BOUNTY_REQUEST);
		hsb.run();
	}

	@Override
	public void runAction(Action a) {
		new StaxDialog(this)
				.setDialogTitle(getString(R.string.bounty_claim_title,
						a.root_code, a.getHumanFriendlyType(this),
						a.getBountyAmountWithCurrency(this) ))
				.setDialogMessage(getString(R.string.bounty_claim_explained, a.getBountyAmountWithCurrency(this), a.getDetailedFullDescription(this)))
				.setPosButton(R.string.start_USSD_Flow, v -> {
					makeCall(a);
				})
				.showIt();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.BOUNTY_REQUEST && resultCode == RESULT_OK) {
			new StaxDialog(this)
					.setDialogTitle(R.string.flow_recorded)
					.setDialogMessage(R.string.bounty_flow_completed_dialog_msg)
					.setPosButton(R.string.go_through_another_flow, null)
					.showIt();
		}
	}
}
