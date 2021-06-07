package com.hover.stax.transfers;

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
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.channels.ChannelDropdown;
import com.hover.stax.channels.ChannelsViewModel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.permissions.PermissionUtils;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.StaxCardView;

public abstract class AbstractFormFragment extends Fragment {
    private static String TAG = "AbstractFormFragment";

    protected AbstractFormViewModel abstractFormViewModel;
    protected ChannelsViewModel channelsViewModel;

    private LinearLayout noworryText, editRequestCard;
    protected StaxCardView editCard, summaryCard;
    protected ChannelDropdown channelDropdown;
    protected ExtendedFloatingActionButton fab;

    protected void init(View root) {
        editCard = root.findViewById(R.id.editCard);
        editRequestCard = root.findViewById(R.id.editRequestCard);
        noworryText = root.findViewById(R.id.noworry_text);
        summaryCard = root.findViewById(R.id.summaryCard);
        fab = root.findViewById(R.id.fab);
        channelDropdown = root.findViewById(R.id.channel_dropdown);
    }

    protected void startObservers(View root) {
        channelDropdown.setListener(channelsViewModel);
        channelDropdown.setObservers(channelsViewModel, getViewLifecycleOwner());
        setupActionDropdownObservers(channelsViewModel, getViewLifecycleOwner());
        abstractFormViewModel.getIsEditing().observe(getViewLifecycleOwner(), this::showEdit);

    }

    private void setupActionDropdownObservers(ChannelsViewModel viewModel, LifecycleOwner lifecycleOwner) {
        viewModel.getActiveChannel().observe(lifecycleOwner, channel -> Log.i(TAG, "Got new active channel: " + channel + " " + channel.countryAlpha2));
        viewModel.getChannelActions().observe(lifecycleOwner, actions -> Log.i(TAG, "Got new actions: " + actions.size()));
    }

    protected void showEdit(boolean isEditing) {
        channelsViewModel.setChannelSelected(channelDropdown.getHighlighted());

        if (editCard != null) editCard.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        if (editRequestCard != null)
            editRequestCard.setVisibility(isEditing ? View.VISIBLE : View.GONE);

        noworryText.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        summaryCard.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        fab.setText(isEditing ? getString(R.string.btn_continue) : abstractFormViewModel.getType().equals(HoverAction.AIRTIME) ? getString(R.string.fab_airtimenow) : getString(R.string.fab_transfernow));
    }

    protected void contactPicker(int requestCode, Context c) {
        Utils.logAnalyticsEvent(getString(R.string.try_contact_select), c);
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
            Utils.logAnalyticsEvent(getString(R.string.contact_perm_success), getContext());
            startContactIntent(requestCode);
        } else {
            Utils.logAnalyticsEvent(getString(R.string.contact_perm_denied), getContext());
            UIHelper.flashMessage(getContext(), getResources().getString(R.string.toast_error_contactperm));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != Constants.ADD_SERVICE && resultCode == Activity.RESULT_OK) {
            StaxContact staxContact = new StaxContact(data, getContext());
            if (staxContact.getPhoneNumber() != null) {
                Utils.logAnalyticsEvent(getString(R.string.contact_select_success), getContext());
                onContactSelected(requestCode, staxContact);
            } else {
                Utils.logAnalyticsEvent(getString(R.string.contact_select_error), getContext());
                UIHelper.flashMessage(getContext(), getResources().getString(R.string.toast_error_contactselect));
            }
        }
    }

    protected abstract void onContactSelected(int requestCode, StaxContact contact);
}