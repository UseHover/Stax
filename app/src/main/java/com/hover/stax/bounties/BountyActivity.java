package com.hover.stax.bounties;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import com.amplitude.api.Amplitude;
import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.HoverParameters;
import com.hover.stax.R;
import com.hover.stax.databinding.ActivityBountyBinding;
import com.hover.stax.navigation.AbstractNavigationActivity;
import com.hover.stax.pushNotification.PushNotificationTopicsInterface;
import com.hover.stax.utils.Utils;

public class BountyActivity extends AbstractNavigationActivity implements PushNotificationTopicsInterface{
    private static final String TAG = "BountyActivity";
    static final String EMAIL_KEY = "email_for_bounties";
    private static final int BOUNTY_REQUEST = 3000;
    public BountyViewModel bountyViewModel;

    private ActivityBountyBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Amplitude.getInstance().logEvent(getString(R.string.visit_screen, TAG));

        bountyViewModel = new ViewModelProvider(this).get(BountyViewModel.class);
        binding = ActivityBountyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpNav();

        if (!Utils.getString(EMAIL_KEY, this).isEmpty())
            navigateToBountyListFragment(getNavController());
        else
            Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_bounty_email)));
    }

    public void makeCall(HoverAction a) {
        Amplitude.getInstance().logEvent(getString(R.string.clicked_run_bounty_session));
      updatePushNotifGroupStatus(a);
        call(a.public_id);
    }
  
  private void updatePushNotifGroupStatus(HoverAction a) {
		joinAllBountiesTopicNotifGroup(this);
		joinByBountyCountryTopicNotifGroup(a.country_alpha2.toUpperCase(), this);

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
        if (requestCode == BOUNTY_REQUEST) {
            if (data != null) {
                String transactionUUID = data.getStringExtra("uuid");
                if (transactionUUID != null)
                    navigateToTransactionDetailsFragment(transactionUUID, getNavController(), true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        NavController controller = getNavController();

        if (controller.getCurrentDestination() != null && controller.getCurrentDestination().getId() == R.id.bountyListFragment)
            navigateThruHome(R.id.navigation_settings);
        else
            super.onBackPressed();
    }
}
