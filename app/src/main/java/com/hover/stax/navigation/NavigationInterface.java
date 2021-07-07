package com.hover.stax.navigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.bounties.BountyActivity;
import com.hover.stax.channels.ChannelsListFragment;
import com.hover.stax.languages.SelectLanguageActivity;
import com.hover.stax.requests.RequestActivity;
import com.hover.stax.transactions.TransactionDetailsFragment;
import com.hover.stax.transfers.TransferActivity;
import com.hover.stax.utils.Constants;

import static com.hover.stax.settings.SettingsFragment.LANG_CHANGE;

public interface NavigationInterface {

    default void navigate(AppCompatActivity activity, int toWhere) {
        navigate(activity, toWhere, null);
    }

    default void navigate(AppCompatActivity activity, int toWhere, Object data) {
        navigate(activity, toWhere, null, data);
    }

    default void navigate(AppCompatActivity activity, int toWhere, Intent intent, Object data) {
        NavHostFragment navHostFragment = (NavHostFragment) activity.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        switch (toWhere) {
            case Constants.NAV_TRANSFER:
                navigateToTransferActivity(HoverAction.P2P, false, intent, activity);
                break;
            case Constants.NAV_AIRTIME:
                navigateToTransferActivity(HoverAction.AIRTIME, false, intent, activity);
                break;
            case Constants.NAV_REQUEST:
                navigateToRequestFragment(activity);
                break;
            case Constants.NAV_HOME:
                navigateToHomeFragment(navController);
                break;
            case Constants.NAV_BALANCE:
                navigateToBalanceFragment(navController);
                break;
            case Constants.NAV_SETTINGS:
                navigateToSettingsFragment(navController);
                break;
            case Constants.NAV_LINK_ACCOUNT:
                navigateToChannelsListFragment(navController, true);
                break;
            case Constants.NAV_LANGUAGE_SELECTION:
                navigateToLanguageSelectionFragment(activity);
                break;
            case Constants.NAV_BOUNTY:
                activity.startActivity(new Intent(activity, BountyActivity.class));
                break;
            default:
                break;
        }
    }

    default void navigateToRequestFragment(Activity activity) {
        activity.startActivityForResult(new Intent(activity, RequestActivity.class), Constants.REQUEST_REQUEST);
    }

    default void navigateToHomeFragment(NavController navController) {
        navController.navigate(R.id.navigation_home);
    }

    default void navigateToSettingsFragment(NavController navController) {
        navController.navigate(R.id.navigation_settings);
    }

    default void navigateToBalanceFragment(NavController navController) {
        navController.navigate(R.id.navigation_balance);
    }

    default void navigateToChannelsListFragment(NavController navController, boolean forceReturnData) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ChannelsListFragment.FORCE_RETURN_DATA, forceReturnData);
        navController.navigate(R.id.navigation_linkAccount, bundle);
    }

    default void navigateToTransferActivity(String type, boolean isFromStaxLink, Intent received, Activity activity) {
        Intent i = new Intent(activity, TransferActivity.class);
        i.setAction(type);
        if (isFromStaxLink)
            i.putExtra(Constants.REQUEST_LINK, received.getExtras().getString(Constants.REQUEST_LINK));

        activity.startActivityForResult(i, Constants.TRANSFER_REQUEST);
    }

    default void navigateToChannelDetailsFragment(int channel_id, NavController navController) {
        Bundle bundle = new Bundle();
        bundle.putInt(TransactionContract.COLUMN_CHANNEL_ID, channel_id);
        navController.navigate(R.id.channelsDetailsFragment, bundle);
    }

    default void navigateToLanguageSelectionFragment(Activity activity) {
        Intent intentLanguage = new Intent(activity, SelectLanguageActivity.class);
        intentLanguage.putExtra(LANG_CHANGE, true);
        activity.startActivity(intentLanguage);
    }

    default void navigateToPinUpdateFragment(int channel_id, Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putInt("channel_id", channel_id);
        NavHostFragment.findNavController(fragment).navigate(R.id.pinUpdateFragment, bundle);
    }

    default void navigateToTransactionDetailsFragment(String uuid, Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putString(TransactionContract.COLUMN_UUID, uuid);
        NavHostFragment.findNavController(fragment).navigate(R.id.transactionDetailsFragment, bundle);
    }

    default void navigateToTransactionDetailsFragment(String uuid, NavController navController, boolean showBountyButton) {
        Bundle bundle = new Bundle();
        bundle.putString(TransactionContract.COLUMN_UUID, uuid);
        bundle.putBoolean(TransactionDetailsFragment.SHOW_BOUNTY_SUBMIT, showBountyButton);
        navController.navigate(R.id.transactionDetailsFragment, bundle);
    }

    default void navigateToScheduleDetailsFragment(int id, Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        NavHostFragment.findNavController(fragment).navigate(R.id.scheduleDetailsFragment, bundle);
    }

    default void navigateToRequestDetailsFragment(int id, Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        NavHostFragment.findNavController(fragment).navigate(R.id.requestDetailsFragment, bundle);
    }

    default void navigateToWebViewFragment(String url, Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        NavHostFragment.findNavController(fragment).navigate(R.id.webviewFragment, bundle);
    }

    default void navigateToBountyListFragment(NavController navController) {
        navController.navigate(R.id.bountyListFragment);
    }
}
