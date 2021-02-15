package com.hover.stax.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.home.MainActivity;
import com.hover.stax.languages.SelectLanguageActivity;
import com.hover.stax.settings.SettingsFragment;
import com.hover.stax.utils.Constants;
import com.hover.stax.requests.RequestActivity;
import com.hover.stax.transfers.TransferActivity;

import static com.hover.stax.settings.SettingsFragment.LANG_CHANGE;

public interface NavigationInterface {

	default void navigate(Activity activity, int toWhere) {
		navigate(activity, toWhere, null);
	}
	default void navigate(Activity activity, int toWhere, Object data) {
		navigate(activity, toWhere, null, data);
	}
	default void navigate(Activity activity, int toWhere, Intent intent, Object data) {
		switch (toWhere) {
			case Constants.NAV_TRANSFER:
				navigateToTransferActivity(Action.P2P, false, intent,activity);
				break;
			case Constants.NAV_AIRTIME:
				navigateToTransferActivity(Action.AIRTIME, false, intent, activity);
				break;
			case Constants.NAV_REQUEST: navigateToRequestFragment(activity);
				break;
			case Constants.NAV_HOME: navigateToHomeFragment(activity);
				break;
			case Constants.NAV_BALANCE: navigateToBalanceFragment(activity);
				break;
			case Constants.NAV_SETTINGS:navigateToSettingsFragment(activity);
				break;
			case Constants.NAV_LINK_ACCOUNT: navigateToLinkAccountFragment(activity);
				break;
			case Constants.NAV_LANGUAGE_SELECTION: navigateToLanguageSelectionFragment(activity);
				break;
			default:
				break;
		}
	}
	default void navigateToRequestFragment(Activity activity) {
		activity.startActivityForResult(new Intent(activity, RequestActivity.class), Constants.REQUEST_REQUEST);
	}
	default void navigateToHomeFragment(Activity activity) {
		Navigation.findNavController(activity, R.id.nav_host_fragment).navigate(R.id.navigation_home);
	}
	default void navigateToSettingsFragment(Activity activity) {
		Navigation.findNavController(activity, R.id.nav_host_fragment).navigate(R.id.navigation_settings);
	}
	default void navigateToBalanceFragment(Activity activity) {
		Navigation.findNavController(activity, R.id.nav_host_fragment).navigate(R.id.navigation_balance);
	}
	default void navigateToLinkAccountFragment(Activity activity) {
		Navigation.findNavController(activity, R.id.nav_host_fragment).navigate(R.id.navigation_linkAccount);
	}
	default void navigateToTransferActivity(String type, boolean isFromStaxLink, Intent received, Activity activity) {
		Intent i = new Intent(activity, TransferActivity.class);
		i.setAction(type);
		if (isFromStaxLink) i.putExtra(Constants.REQUEST_LINK, received.getExtras().getString(Constants.REQUEST_LINK));
		activity.startActivityForResult(i, Constants.TRANSFER_REQUEST);
	}
	default void navigateToChannelDetailsFragment(int channel_id, Activity activity) {
		Bundle bundle = new Bundle();
		bundle.putInt(TransactionContract.COLUMN_CHANNEL_ID, channel_id);
		Navigation.findNavController(activity.findViewById(R.id.nav_host_fragment)).navigate(R.id.channelsDetailsFragment, bundle);
	}
	default void navigateToLanguageSelectionFragment(Activity activity) {
		Intent intentLanguage = new Intent(activity.getBaseContext(), SelectLanguageActivity.class);
		intentLanguage.putExtra(LANG_CHANGE, true);
		activity.startActivity(intentLanguage);
	}
	default void navigateToPinUpdateFragment(int channel_id, Fragment fragment) {
		Bundle bundle = new Bundle();
		bundle.putInt("channel_id", channel_id);
		NavHostFragment.findNavController(fragment).navigate(R.id.pinUpdateFragment, bundle);
	}
	default  void navigateToMainActivity(Activity activity) {
		activity.startActivity(new Intent(activity, MainActivity.class));
	}
	default void navigateToMainActivityAndRedirectToAFragment(Activity activity, int redirectToWhere) {
		Intent intent = new Intent(activity, MainActivity.class);
		intent.putExtra(Constants.FRAGMENT_DIRECT, redirectToWhere);
		activity.startActivity(intent);
	}
	default void navigateToTransactionDetailsFragment(String uuid, Fragment fragment) {
		Bundle bundle = new Bundle();
		bundle.putString(TransactionContract.COLUMN_UUID, uuid);
		NavHostFragment.findNavController(fragment).navigate(R.id.transactionDetailsFragment, bundle);
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

}
