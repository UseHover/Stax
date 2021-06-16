package com.hover.stax.home;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.balances.BalanceAdapter;
import com.hover.stax.balances.BalancesViewModel;
import com.hover.stax.channels.Channel;
import com.hover.stax.databinding.ActivityMainBinding;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.navigation.AbstractNavigationActivity;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.settings.BiometricChecker;
import com.hover.stax.transactions.TransactionHistoryViewModel;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.List;

import timber.log.Timber;

public class MainActivity extends AbstractNavigationActivity implements
        BalancesViewModel.RunBalanceListener, BalanceAdapter.BalanceListener, BiometricChecker.AuthListener {

    final public static String TAG = "MainActivity";
    private BalancesViewModel balancesViewModel;

    private ActivityMainBinding binding;
    TransactionHistoryViewModel transactionHistoryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        balancesViewModel = new ViewModelProvider(this).get(BalancesViewModel.class);
        transactionHistoryViewModel  = new ViewModelProvider(this).get(TransactionHistoryViewModel.class);
        balancesViewModel.setListener(this);
        balancesViewModel.getSelectedChannels().observe(this, channels -> Timber.i("Channels observer is necessary to make updates fire, but all logic is in viewmodel. %s", channels.size()));
        balancesViewModel.getToRun().observe(this, actions -> Timber.i("RunActions observer is necessary to make updates fire, but all logic is in viewmodel. %s", actions.size()));
        balancesViewModel.getRunFlag().observe(this, flag -> Timber.i("Flag observer is necessary to make updates fire, but all logic is in viewmodel. %s", flag));
        balancesViewModel.getActions().observe(this, actions -> Timber.i("Actions observer is necessary to make updates fire, but all logic is in viewmodel. %s", actions.size()));

        checkForRequest(getIntent());
        checkForFragmentDirection(getIntent());
        checkForDeepLinking();
        observeForAppReview();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkForRequest(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setUpNav();
    }

    private void checkForDeepLinking() {
        Intent intent = getIntent();
        if(intent.getAction()!=null && intent.getAction().equals(Intent.ACTION_VIEW) && intent.getData() != null) {
            String deepLinkRoute = intent.getData().toString();
            if(deepLinkRoute.contains(getString(R.string.deeplink_sendmoney))) {
                navigateToTransferActivity(HoverAction.P2P, false, intent, this);
            }
            else if(deepLinkRoute.contains(getString(R.string.deeplink_airtime))) {
                navigateToTransferActivity(HoverAction.AIRTIME, false, intent, this);
            }
            else if(deepLinkRoute.contains(getString(R.string.deeplink_linkaccount))) {
                navigateToChannelsListFragment(getNavController(), true);
            }
            else if(deepLinkRoute.contains(getString(R.string.deeplink_balance)) || deepLinkRoute.contains(getString(R.string.deeplink_history))) {
                navigateToBalanceFragment(getNavController());
            }
            else if(deepLinkRoute.contains(getString(R.string.deeplink_settings))) {
                navigateToSettingsFragment(getNavController());
            }
            else if(deepLinkRoute.contains(getString(R.string.deeplink_reviews))) {
                Timber.i("Currently at reviews 1");
                launchStaxReview();
            }
        }
    }
    private void observeForAppReview() {
        transactionHistoryViewModel.showAppReviewLiveData().observe(this, status -> {
            if(status) { launchRatingAndReviewDialog(); }
        });
    }

    private void launchStaxReview() {
        Utils.logAnalyticsEvent(getString(R.string.visited_rating_review_screen), this);
        if(Utils.getBoolean(Constants.APP_RATED_NATIVELY, this)) openStaxPlaystorePage();
        else launchRatingAndReviewDialog();
    }

    private void launchRatingAndReviewDialog() {
        Timber.i("Currently at reviews native");
        ReviewManager reviewManager = ReviewManagerFactory.create(this);
        reviewManager.requestReviewFlow().addOnCompleteListener(task -> {
            Timber.i("Currently at reviews native already completed");
            if(task.isSuccessful()){
                Timber.i("Currently at reviews native already succeeded");
                reviewManager.launchReviewFlow(MainActivity.this, task.getResult()).addOnCompleteListener(
                        task1 -> Utils.saveBoolean(Constants.APP_RATED_NATIVELY, true, MainActivity.this));
            }
        });
    }

    private void openStaxPlaystorePage() {
        Timber.i("Currently at reviews for external store page");
        Uri link = Uri.parse(getString(R.string.stax_market_playstore_link));
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, link);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.stax_url_playstore_review_link))));
        }
    }

    private void checkForRequest(Intent intent) {
        if (intent.hasExtra(Constants.REQUEST_LINK))
            navigateToTransferActivity(HoverAction.P2P, true, intent, this);
    }

    private void checkForFragmentDirection(Intent intent) {
        if (intent.hasExtra(Constants.FRAGMENT_DIRECT)) {
            int toWhere = intent.getExtras().getInt(Constants.FRAGMENT_DIRECT, 0);
            Timber.e("Dest %s", getNavConst(toWhere));

            checkPermissionsAndNavigate(toWhere);
        }
    }

    @Override
    public void onTapDetail(int channel_id) {
        if(channel_id == Channel.DUMMY) navigateToChannelsListFragment(getNavController(), true);
        else navigateToChannelDetailsFragment(channel_id, getNavController());
    }

    @Override
    public void onTapRefresh(int channel_id) {
        if(channel_id == Channel.DUMMY) navigateToChannelsListFragment(getNavController(), false);
        else{
            Utils.logAnalyticsEvent(getString(R.string.refresh_balance_single), this);
            balancesViewModel.setRunning(channel_id);
        }

    }

    @Override
    public void startRun(HoverAction a, int i) {
        run(a, i);
    }

    private void run(HoverAction action, int index) {
        Timber.e("running index: %s", index);

        if (balancesViewModel.getChannel(action.channel_id) != null) {
            HoverSession.Builder hsb = new HoverSession.Builder(action, balancesViewModel.getChannel(action.channel_id), MainActivity.this, index);
            if (index + 1 < balancesViewModel.getSelectedChannels().getValue().size())
                hsb.finalScreenTime(0);
            hsb.run();
        } else { // Fix for auth issue on OnePlus 6
            new Handler(Looper.getMainLooper()).post(() -> {
                balancesViewModel.getSelectedChannels().observe(MainActivity.this, new Observer<List<Channel>>() {
                    @Override
                    public void onChanged(List<Channel> channels) {
                        if (channels != null && balancesViewModel.getChannel(channels, action.channel_id) != null) {
                            run(action, 0);
                            balancesViewModel.getSelectedChannels().removeObserver(this);
                        }
                    }
                });
            });
        }
    }

    @Override
    public void onAuthError(String error) {
        Timber.e("error: %s", error);
    }

    @Override
    public void onAuthSuccess(HoverAction act) {
        run(act, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.TRANSFER_REQUEST:
                if (data != null) {
                    onProbableHoverCall(data);
                }
                break;
            case Constants.REQUEST_REQUEST:
                if (resultCode == RESULT_OK) {
                    onRequest(data);
                }
                break;
            default: // requestCode < Constants.BALANCE_MAX // Balance call
                balancesViewModel.setRan(requestCode);
                if (resultCode == RESULT_OK && data != null && data.getAction() != null)
                    onProbableHoverCall(data);
        }
    }

    private void onProbableHoverCall(Intent data) {
        if (data.getAction() != null && data.getAction().equals(Constants.SCHEDULED)) {
            showMessage(getString(R.string.toast_confirm_schedule, DateUtils.humanFriendlyDate(data.getLongExtra(Schedule.DATE_KEY, 0))));
        } else {
            Utils.logAnalyticsEvent(getString(R.string.finish_load_screen), this);
            new ViewModelProvider(this).get(TransactionHistoryViewModel.class).saveTransaction(data, this);
        }
    }


    private void onRequest(Intent data) {
        if (data.getAction().equals(Constants.SCHEDULED))
            showMessage(getString(R.string.toast_request_scheduled, DateUtils.humanFriendlyDate(data.getLongExtra(Schedule.DATE_KEY, 0))));
        else
            showMessage(getString(R.string.toast_confirm_request));
    }

    private void showMessage(String str) {
        UIHelper.flashMessage(this, findViewById(R.id.fab), str);
    }
}