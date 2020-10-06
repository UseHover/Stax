package com.hover.stax.requests;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.transfers.StaxContactModel;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import static com.hover.stax.transfers.TransferFragment.READ_CONTACT;

public class RequestFragment extends Fragment implements RequestFromWhoInputAdapter.ContactClickListener {

	private RequestViewModel requestViewModel;
	private int stage;
	private int recentClickedTag;

	private RecyclerView fromWhoInputRecyclerView;
	private RequestFromWhoInputAdapter requestFromWhoInputAdapter;
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		requestViewModel = new ViewModelProvider(this).get(RequestViewModel.class);
		return inflater.inflate(R.layout.fragment_request, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init(view);

		requestViewModel.getIntendingRequests().observe(getViewLifecycleOwner(), requests -> {

				requestFromWhoInputAdapter = new RequestFromWhoInputAdapter(requests, RequestFragment.this);
				fromWhoInputRecyclerView.setAdapter(requestFromWhoInputAdapter);

		});
	}

	private void init(View view) {
		fromWhoInputRecyclerView = view.findViewById(R.id.fromWhoRecyclerView);
		fromWhoInputRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));



		view.findViewById(R.id.add_someoneElse_button).setOnClickListener(v -> {
			requestViewModel.addRequest();
		});
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == READ_CONTACT && resultCode == Activity.RESULT_OK) {
			StaxContactModel staxContactModel = new StaxContactModel(data);
			if (staxContactModel.getPhoneNumber() != null) {
				Amplitude.getInstance().logEvent(getString(R.string.contact_select_success));
				requestViewModel.updateRequestRecipient(recentClickedTag, staxContactModel.getPhoneNumber());
			} else {
				Amplitude.getInstance().logEvent(getString(R.string.contact_select_error));
				UIHelper.flashMessage(getContext(), getResources().getString(R.string.selectContactErrorMessage));
			}
		}
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == READ_CONTACT && new PermissionHelper().permissionsGranted(grantResults)) {
			Amplitude.getInstance().logEvent(getString(R.string.contact_perm_success));
			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(contactPickerIntent, READ_CONTACT);
		} else {
			Amplitude.getInstance().logEvent(getString(R.string.contact_perm_denied));
			UIHelper.flashMessage(getContext(), getResources().getString(R.string.contact_perm_error));
		}
	}

	@Override
	public void onClick(int tag) {
		recentClickedTag = tag;
		Amplitude.getInstance().logEvent(getString(R.string.try_contact_select));
		if (PermissionUtils.hasContactPermission()) {
			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(contactPickerIntent, READ_CONTACT);
		} else {
			requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACT);
		}
	}

	@Override
	public void onEditText(int tag, String content) {
		requestViewModel.updateRequestRecipientNoUISync(tag, content);
	}
}
