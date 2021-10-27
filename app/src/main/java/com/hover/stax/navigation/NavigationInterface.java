package com.hover.stax.navigation;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.stax.R;
import com.hover.stax.bounties.BountyActivity;
import com.hover.stax.channels.AddChannelsFragment;
import com.hover.stax.transactions.TransactionDetailsFragment;
import com.hover.stax.transfers.TransferActivity;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.UIHelper;

import timber.log.Timber;

public interface NavigationInterface {

    default void navigate(AppCompatActivity activity, int toWhere) {
        NavHostFragment navHostFragment = (NavHostFragment) activity.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        switch (toWhere) {
            case Constants.NAV_TRANSFER:
                navigateToTransferFragment(navController, HoverAction.P2P);
                break;
            case Constants.NAV_AIRTIME:
                navigateToTransferFragment(navController, HoverAction.AIRTIME);
                break;
            case Constants.NAV_REQUEST:
                navigateToRequestFragment(navController);
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
            case Constants.NAV_EMAIL_CLIENT:
                openSupportEmailClient(activity);
                break;
            case Constants.NAV_USSD_LIB:
                navigateToUSSDLib(navController);
            default:
                break;
        }
    }

    default void navigateToUSSDLib(NavController navController) {
        navController.navigate(R.id.libraryFragment);
    }

    default void navigateToRequestFragment(NavController navController) {
        navController.navigate(R.id.navigation_request);
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
        bundle.putBoolean(AddChannelsFragment.FORCE_RETURN_DATA, forceReturnData);
        navController.navigate(R.id.action_navigation_home_to_navigation_linkAccount, bundle);
    }

    default void navigateToTransferFragment(NavController navController, String actionType) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TRANSACTION_TYPE, actionType);
        navController.navigate(R.id.action_navigation_home_to_navigation_transfer, bundle);
    }

    default void navigateToTransactionDetailsFragment(String uuid, FragmentManager manager, Boolean isFullScreen) {
        TransactionDetailsFragment frag = TransactionDetailsFragment.Companion.newInstance(uuid, isFullScreen);
        frag.show(manager, "dialogFrag");
    }

    default void navigateToBountyListFragment(NavController navController) {
        navController.navigate(R.id.bountyListFragment);
    }

    default void openSupportEmailClient(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String recipientEmail = activity.getString(R.string.stax_support_email);
        String subject = activity.getString(R.string.stax_emailing_subject, Hover.getDeviceId(activity.getBaseContext()));

        Uri data = Uri.parse("mailto:" + recipientEmail + " ?subject=" + subject);
        intent.setData(data);

        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Timber.e("Activity not found");
            UIHelper.flashMessage(activity, activity.getString(R.string.email_client_not_found));
        }
    }
}