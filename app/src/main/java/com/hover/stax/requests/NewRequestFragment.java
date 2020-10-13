package com.hover.stax.requests;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.transfers.StaxContactModel;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.ArrayList;

import static android.view.View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION;
import static com.hover.stax.requests.RequestStage.RECIPIENT;

public class NewRequestFragment extends Fragment {

	private NewRequestViewModel requestViewModel;

	private LinearLayout recipientInputList, recipientValueList;
	private RelativeLayout firstRecipient;
	private TextView amountValue, noteValue;
	private EditText amountInput, noteInput;
	private Button addRecipientBtn;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		requestViewModel = new ViewModelProvider(requireActivity()).get(NewRequestViewModel.class);
		View view = inflater.inflate(R.layout.fragment_request, container, false);
		init(view);
		startObservers(view);
		startListeners(view);
		return view;
	}

	private void init(View view) {
		recipientValueList = view.findViewById(R.id.recipientValueList);
		amountValue = view.findViewById(R.id.amountValue);
		noteValue = view.findViewById(R.id.noteValue);
		recipientInputList = view.findViewById(R.id.recipient_list);
		firstRecipient = view.findViewById(R.id.firstRecipient);
		amountInput = view.findViewById(R.id.amount_input);
		noteInput = view.findViewById(R.id.note_input);
		addRecipientBtn = view.findViewById(R.id.add_recipient_button);
	}

	private void startObservers(View root) {
		requestViewModel.getStage().observe(getViewLifecycleOwner(), this::validate);
		requestViewModel.getRecipients().observe(getViewLifecycleOwner(), recipients -> {
			if (recipients == null || recipients.size() == 0) return;
			recipientValueList.removeAllViews();
			for (String recipient : recipients) {
				TextView tv = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.recipient_cell, null);
				tv.setText(recipient);
				recipientValueList.addView(tv);
			}
		});
		requestViewModel.getAmount().observe(getViewLifecycleOwner(), amount -> {
			amountInput.setText(amount);
			amountValue.setText(Utils.formatAmount(amount));
		});
		requestViewModel.getNote().observe(getViewLifecycleOwner(), note -> {
			noteInput.setText(note);
			noteValue.setText(note);
		});

		requestViewModel.getIsFuture().observe(getViewLifecycleOwner(), isFuture -> root.findViewById(R.id.dateInput).setVisibility(isFuture ? View.VISIBLE : View.GONE));
		requestViewModel.getFutureDate().observe(getViewLifecycleOwner(), futureDate -> {
			((TextView) root.findViewById(R.id.dateInput)).setText(futureDate != null ? DateUtils.humanFriendlyDate(futureDate) : getString(R.string.date));
			((TextView) root.findViewById(R.id.dateValue)).setText(futureDate != null ? DateUtils.humanFriendlyDate(futureDate) : getString(R.string.date));
		});
	}

	private void startListeners(View view) {
		firstRecipient.findViewById(R.id.contact_button).setOnClickListener(btn -> onClickContact(btn, 0));
		addRecipientBtn.setOnClickListener(v -> {
			View input = LayoutInflater.from(getContext()).inflate(R.layout.recipient_input, null);
			final int childIndex = recipientInputList.getChildCount();
			input.findViewById(R.id.contact_button).setOnClickListener(btn -> onClickContact(btn, childIndex));
			recipientInputList.addView(input);
		});
	}

	private void onClickContact(View v, int index) {
		Amplitude.getInstance().logEvent(getString(R.string.try_contact_select));
		if (PermissionUtils.hasContactPermission(v.getContext())) {
			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(contactPickerIntent, index);
		} else {
			requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, index);
		}
	}

	private void validate(RequestStage stage) {
		switch (stage) {
			case AMOUNT:
				if (validates(firstRecipient.findViewById(R.id.recipient_input), RECIPIENT, R.string.enterRecipientError)) {
					ArrayList<View> outputViews = new ArrayList<>();
					recipientInputList.findViewsWithText(outputViews, "RECIPIENT_INPUT", FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
					for (View view : outputViews) {
						if (!((TextInputEditText) view).getText().toString().isEmpty())
							requestViewModel.addRecipient(((TextInputEditText) view).getText().toString());
					}
				}
				amountInput.requestFocus();
				break;
			case NOTE:
				if (!amountInput.getText().toString().isEmpty())
					requestViewModel.setAmount(amountInput.getText().toString());
				noteInput.requestFocus();
				break;
			case REVIEW:
				if (!noteInput.getText().toString().isEmpty())
					requestViewModel.setNote(noteInput.getText().toString());
				break;
		}
	}

	private boolean validates(EditText input, RequestStage stage, int errorMsg) {
		if (input.getText().toString().isEmpty()) {
			boolean canGoBack = requestViewModel.goToStage(stage);
			if (canGoBack)
				((TextInputLayout) input.getParent().getParent()).setError(getString(errorMsg));
			return !canGoBack;
		}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			StaxContactModel staxContactModel = new StaxContactModel(data, getContext());
			if (staxContactModel.getPhoneNumber() != null) {
				Amplitude.getInstance().logEvent(getString(R.string.contact_select_success));
				((TextInputEditText) recipientInputList.getChildAt(requestCode).findViewById(R.id.recipient_input))
						.setText(staxContactModel.getPhoneNumber());
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
}
