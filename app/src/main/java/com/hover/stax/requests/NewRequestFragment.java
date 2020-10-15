package com.hover.stax.requests;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.security.PinEntryAdapter;
import com.hover.stax.transfers.StaxContactModel;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.StagedFragment;
import com.hover.stax.utils.StagedViewModel;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.ArrayList;

import static android.view.View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION;
import static com.hover.stax.requests.RequestStage.AMOUNT;
import static com.hover.stax.requests.RequestStage.NOTE;
import static com.hover.stax.requests.RequestStage.RECIPIENT;

public class NewRequestFragment extends StagedFragment implements RecipientAdapter.UpdateListener {

	protected NewRequestViewModel requestViewModel;

	private RecyclerView recipientInputList;
	private LinearLayout recipientValueList;
	private TextView amountValue, noteValue;
	private EditText amountInput, noteInput;
	private Button addRecipientBtn;

	private RecipientAdapter recipientAdapter;
	private int recipientCount = 0;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		stagedViewModel = new ViewModelProvider(requireActivity()).get(NewRequestViewModel.class);
		requestViewModel = (NewRequestViewModel) stagedViewModel;
		View view = inflater.inflate(R.layout.fragment_request, container, false);
		init(view);
		return view;
	}

	@Override
	protected void init(View view) {
		recipientValueList = view.findViewById(R.id.recipientValueList);
		amountValue = view.findViewById(R.id.amountValue);
		noteValue = view.findViewById(R.id.noteValue);
		recipientInputList = view.findViewById(R.id.recipient_list);
		amountInput = view.findViewById(R.id.amount_input);
		noteInput = view.findViewById(R.id.note_input);
		addRecipientBtn = view.findViewById(R.id.add_recipient_button);

		recipientInputList.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
		recipientAdapter = new RecipientAdapter(null, this);
		recipientInputList.setAdapter(recipientAdapter);

		super.init(view);
	}

	@Override
	protected void startObservers(View root) {
		super.startObservers(root);
		requestViewModel.getStage().observe(getViewLifecycleOwner(), stage -> {
			switch ((RequestStage) stage) {
				case AMOUNT: amountInput.requestFocus(); break;
				case NOTE: noteInput.requestFocus(); break;
			}
		});

		requestViewModel.getRecipients().observe(getViewLifecycleOwner(), recipients -> {
			if (recipients == null || recipients.size() == 0) return;
			recipientValueList.removeAllViews();
			for (String recipient : recipients) {
				TextView tv = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.recipient_cell, null);
				tv.setText(recipient);
				recipientValueList.addView(tv);
			}

			if (recipients.size() == recipientCount) return;
			recipientCount = recipients.size();
			recipientAdapter.update(recipients);

		});
		requestViewModel.getRecipientError().observe(getViewLifecycleOwner(), recipientError -> {
			TextInputLayout v = recipientInputList.getChildAt(0).findViewById(R.id.recipientEntry);
			v.setError((recipientError != null ? getString(recipientError) : null));
			v.setErrorIconDrawable(0);
		});
		requestViewModel.getAmount().observe(getViewLifecycleOwner(), amount -> amountValue.setText(Utils.formatAmount(amount)));
		requestViewModel.getNote().observe(getViewLifecycleOwner(), note -> noteValue.setText(note));
	}

	@Override
	protected void startListeners(View root) {
		super.startListeners(root);
		addRecipientBtn.setOnClickListener(v -> requestViewModel.addRecipient(""));
		amountInput.addTextChangedListener(amountWatcher);
		noteInput.addTextChangedListener(noteWatcher);
	}

	@Override
	public void onUpdate(int pos, String recipient) { requestViewModel.onUpdate(pos, recipient); }

	@Override
	public void onClickContact(int index, Context c) {
		Amplitude.getInstance().logEvent(getString(R.string.try_contact_select));
		if (PermissionUtils.hasContactPermission(c)) {
			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(contactPickerIntent, index);
		} else {
			requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, index);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			StaxContactModel staxContactModel = new StaxContactModel(data, getContext());
			if (staxContactModel.getPhoneNumber() != null) {
				Amplitude.getInstance().logEvent(getString(R.string.contact_select_success));
				requestViewModel.onUpdate(requestCode, staxContactModel.getPhoneNumber());
				recipientAdapter.notifyDataSetChanged();
			} else {
				Amplitude.getInstance().logEvent(getString(R.string.contact_select_error));
				UIHelper.flashMessage(getContext(), getResources().getString(R.string.selectContactErrorMessage));
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (new PermissionHelper(getContext()).permissionsGranted(grantResults)) {
			Amplitude.getInstance().logEvent(getString(R.string.contact_perm_success));
			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(contactPickerIntent, requestCode);
		} else {
			Amplitude.getInstance().logEvent(getString(R.string.contact_perm_denied));
			UIHelper.flashMessage(getContext(), getResources().getString(R.string.contact_perm_error));
		}
	}

	private TextWatcher amountWatcher = new TextWatcher() {
		@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
		@Override public void afterTextChanged(Editable editable) { }

		@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			requestViewModel.setAmount(charSequence.toString());
		}
	};

	private TextWatcher noteWatcher = new TextWatcher() {
		@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
		@Override public void afterTextChanged(Editable editable) { }

		@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			requestViewModel.setNote(charSequence.toString());
		}
	};
}
