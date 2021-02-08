package com.hover.stax.utils.abstractClasses;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.amplitude.api.Amplitude;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.ChannelDropdown;
import com.hover.stax.channels.ChannelDropdownViewModel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.Constants;
import com.hover.stax.permissions.PermissionUtils;
import com.hover.stax.utils.AbstractFormViewModel;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.StaxCardView;

public abstract class AbstractFormFragment extends Fragment {
	private static String TAG = "AbstractFormFragment";

	protected AbstractFormViewModel abstractFormViewModel;
	protected ChannelDropdownViewModel channelDropdownViewModel;

	private LinearLayout noworryText;
	protected StaxCardView editCard, summaryCard;
	protected ChannelDropdown channelDropdown;
	protected ExtendedFloatingActionButton fab;

	protected void init(View root) {
		editCard = root.findViewById(R.id.editCard);
		noworryText = root.findViewById(R.id.noworry_text);
		summaryCard = root.findViewById(R.id.summaryCard);
		fab = root.findViewById(R.id.fab);
		channelDropdown = root.findViewById(R.id.channel_dropdown);
	}

	protected void startObservers(View root) {
		channelDropdown.setListener(channelDropdownViewModel);
		channelDropdownViewModel.getSims().observe(getViewLifecycleOwner(), sims -> Log.i(TAG, "Got sims: " + sims.size()));
		channelDropdownViewModel.getSimHniList().observe(getViewLifecycleOwner(), simList -> Log.i(TAG, "Got new sim hni list: " + simList));
		channelDropdownViewModel.getSimChannels().observe(getViewLifecycleOwner(), channels -> channelDropdown.updateChannels(channels));
		channelDropdownViewModel.getChannels().observe(getViewLifecycleOwner(), channels -> channelDropdown.updateChannels(channels));
		channelDropdownViewModel.getSimChannels().observe(getViewLifecycleOwner(), channels -> channelDropdown.updateChannels(channels));
		channelDropdownViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> Log.i(TAG, "Got selected channels: " + channels.size()));
		channelDropdownViewModel.getActiveChannel().observe(getViewLifecycleOwner(), channel -> Log.i(TAG, "Got new active channel: " + channel + " " + channel.countryAlpha2));
		channelDropdownViewModel.getChannelActions().observe(getViewLifecycleOwner(), actions -> Log.i(TAG, "Got new actions: " + actions.size()));
		channelDropdownViewModel.getError().observe(getViewLifecycleOwner(), error -> channelDropdown.setError(error));
		channelDropdownViewModel.getHelper().observe(getViewLifecycleOwner(), helper -> channelDropdown.setHelper(helper != null ? getString(helper) : null));
		abstractFormViewModel.getIsEditing().observe(getViewLifecycleOwner(), this::showEdit);
	}

	protected void showEdit(boolean isEditing) {
		channelDropdownViewModel.setChannelSelected(channelDropdown.getHighlighted());
		editCard.setVisibility(isEditing ? View.VISIBLE : View.GONE);
		noworryText.setVisibility(isEditing ? View.VISIBLE : View.GONE);
		summaryCard.setVisibility(isEditing ? View.GONE : View.VISIBLE);
		fab.setText(isEditing ? getString(R.string.btn_continue) : abstractFormViewModel.getType().equals(Action.AIRTIME) ? getString(R.string.fab_airtimenow) : getString(R.string.fab_transfernow));
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
