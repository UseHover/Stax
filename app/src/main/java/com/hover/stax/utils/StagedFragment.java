package com.hover.stax.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.amplitude.api.Amplitude;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDropdown;
import com.hover.stax.channels.ChannelDropdownViewModel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.Constants;
import com.hover.stax.permissions.PermissionUtils;
import com.hover.stax.views.Stax2LineItem;

public abstract class StagedFragment extends Fragment implements ChannelDropdown.HighlightListener {
	private static String TAG = "StagedFragment";

	protected ChannelDropdownViewModel channelDropdownViewModel;
	protected ChannelDropdown channelDropdown;

	protected Stax2LineItem accountValue;

	protected void init(View root) {
		channelDropdown = root.findViewById(R.id.channel_dropdown);
		channelDropdown.setListener(this);
		channelDropdownViewModel.getChannels().observe(getViewLifecycleOwner(), channels -> channelDropdown.updateChannels(channels));
		channelDropdownViewModel.getSimChannels().observe(getViewLifecycleOwner(), channels -> channelDropdown.updateChannels(channels));
		channelDropdownViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> Log.e(TAG, "Got selected channels: " + channels.size()));
		channelDropdownViewModel.getActiveChannel().observe(getViewLifecycleOwner(), channel -> Log.e(TAG, "Got new active channel: " + channel));
		channelDropdownViewModel.getActions().observe(getViewLifecycleOwner(), actions -> Log.e(TAG, "Got new actions: " + actions.size()));
	}

	@Override
	public void highlightChannel(Channel c) {
		channelDropdownViewModel.setActiveChannel(c);
	}

	protected void contactPicker(int requestCode, Context c) {
		Amplitude.getInstance().logEvent(getString(R.string.try_contact_select));
		if (PermissionUtils.hasContactPermission(c))
			startContactIntent(requestCode);
		else
			requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, requestCode);
	}

	private void startContactIntent(int requestCode) {
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(contactPickerIntent, requestCode);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (new PermissionHelper(getContext()).permissionsGranted(grantResults)) {
			Amplitude.getInstance().logEvent(getString(R.string.contact_perm_success));
			startContactIntent(requestCode);
		} else {
			Amplitude.getInstance().logEvent(getString(R.string.contact_perm_denied));
			UIHelper.flashMessage(getContext(), getResources().getString(R.string.toast_error_contactperm));
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode != Constants.ADD_SERVICE && resultCode == Activity.RESULT_OK) {
			StaxContact staxContact = new StaxContact(data, getContext());
			if (staxContact.getPhoneNumber() != null) {
				Amplitude.getInstance().logEvent(getString(R.string.contact_select_success));
				onContactSelected(requestCode, staxContact);
			} else {
				Amplitude.getInstance().logEvent(getString(R.string.contact_select_error));
				UIHelper.flashMessage(getContext(), getResources().getString(R.string.toast_error_contactselect));
			}
		}
	}

	protected abstract void onContactSelected(int requestCode, StaxContact contact);
}
