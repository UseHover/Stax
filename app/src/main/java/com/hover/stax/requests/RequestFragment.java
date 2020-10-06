package com.hover.stax.requests;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.transfers.StaxContactModel;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.List;

import static com.hover.stax.transfers.TransferFragment.READ_CONTACT;

public class RequestFragment extends Fragment implements RequestRecipientAdapter.ContactClickListener {

	private RequestViewModel requestViewModel;
	private int recentClickedTag;

	private RecyclerView fromWhoInputRecyclerView;
	private RequestRecipientAdapter requestRecipientAdapter;
	private CardView amountStage, messageStage;
	private RequestStage nextInputStage = RequestStage.ENTER_RECIPIENT;
	private RequestStage previousInputStage = null;

	private TextView fromWhoLabel, fromWhoValue, amountLabel, amountValue, messageLabel, messageValue, sms_notice;
	private EditText amountInput, messageInput;
	private Button addSomeElse;

	private final static int SEND_SMS = 202;

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

			requestRecipientAdapter = new RequestRecipientAdapter(requests, RequestFragment.this);
			fromWhoInputRecyclerView.setAdapter(requestRecipientAdapter);

		});
	}

	private void init(View view) {
		amountStage = view.findViewById(R.id.amountStageCardView);
		messageStage = view.findViewById(R.id.messageStageCardView);
		fromWhoLabel = view.findViewById(R.id.fromWhoLabel);
		fromWhoValue = view.findViewById(R.id.fromWhoValue);
		amountLabel = view.findViewById(R.id.amountLabel);
		amountValue = view.findViewById(R.id.amountValue);
		messageLabel = view.findViewById(R.id.messageLabel);
		messageValue = view.findViewById(R.id.messageValue);
		amountInput = view.findViewById(R.id.amount_input);
		messageInput = view.findViewById(R.id.message_input);
		addSomeElse = view.findViewById(R.id.add_someoneElse_button);
		sms_notice = view.findViewById(R.id.sms_notice);

		fromWhoInputRecyclerView = view.findViewById(R.id.fromWhoRecyclerView);
		fromWhoInputRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));

		handleRequestStages();
		handleBackButton(view);
		handleContinueButton(view);

		addSomeElse.setOnClickListener(v -> { requestViewModel.addRequest(); });

	}

	private void handleBackButton(View root) {
		root.findViewById(R.id.backButton).setOnClickListener(v -> {
			if (previousInputStage != null) requestViewModel.setRequestStage(previousInputStage);
			else if (getActivity() != null) getActivity().onBackPressed();
		});
	}

	private void handleRequestStages() {
		requestViewModel.getStage().observe(getViewLifecycleOwner(), this::updateStage);
	}

	private void sendRequest() {
		if (PermissionUtils.hasContactPermission()) {
			try {
				String[] phones = fromWhoValue.getText().toString().split(",");
				SmsManager smsManager = SmsManager.getDefault();
				String content = getContext().getString(R.string.request_money_sms_template, amountValue.getText(), messageValue.getText());
				for (String phone : phones) {
					smsManager.sendTextMessage(phone, null, content, null, null);
				}

				startActivity(new Intent(getActivity(), ProcessingActivity.class));
			} catch (Exception ex) {
				UIHelper.flashMessage(getContext(), ex.getMessage());
				ex.printStackTrace();
			}
		} else requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SEND_SMS);
	}

	private void handleContinueButton(View view) {
		view.findViewById(R.id.continue_request_button).setOnClickListener(v -> {
			if (nextInputStage == RequestStage.SEND) {
				requestViewModel.saveCopyToDatabase();
				sendRequest();
			} else {
				requestViewModel.setRequestStage(nextInputStage);

				if (nextInputStage == RequestStage.AMOUNT) {
					List<Request> requests = requestViewModel.getIntendingRequests().getValue();
					if (requests != null) {
						StringBuilder string = new StringBuilder();
						for (Request request : requests) {
							string.append(", ").append(request.recipient);
						}
						String finalizedString = string.toString().replaceFirst(",", "");
						fromWhoValue.setText(finalizedString);
					}

				} else if (nextInputStage == RequestStage.MESSAGE) {
					if (TextUtils.getTrimmedLength(amountInput.getText()) > 0) {
						amountValue.setText(Utils.formatAmount(amountInput.getText().toString()));
						requestViewModel.updateRequestAmount(amountInput.getText().toString());
					}
				} else if (nextInputStage == RequestStage.REVIEW) {
					if (TextUtils.getTrimmedLength(messageInput.getText()) > 0) {
						messageValue.setText(messageInput.getText().toString());
						requestViewModel.updateRequestMessage(messageInput.getText().toString());
					}
				}
			}
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

	private void setRecipientInputViewStage(int v) {
		fromWhoInputRecyclerView.setVisibility(v);
		addSomeElse.setVisibility(v);
	}

	private void setAmountStage(int v) {
		amountStage.setVisibility(v);
	}

	private void setMessageStage(int v) {
		messageStage.setVisibility(v);
	}

	private void setAmountReview(int v) {
		amountLabel.setVisibility(v);
		amountValue.setVisibility(v);
	}

	private void setMessageReview(int v) {
		messageLabel.setVisibility(v);
		messageValue.setVisibility(v);
	}

	private void setFromWhoReview(int v) {
		fromWhoLabel.setVisibility(v);
		fromWhoValue.setVisibility(v);

	}

	private void updateStage(RequestStage stage) {
		switch (stage) {
			case ENTER_RECIPIENT:
				setFromWhoReview(View.GONE);
				setAmountReview(View.GONE);
				setMessageReview(View.GONE);


				setRecipientInputViewStage(View.VISIBLE);
				setAmountStage(View.GONE);
				setMessageStage(View.GONE);

				previousInputStage = null;
				nextInputStage = RequestStage.AMOUNT;

				sms_notice.setVisibility(View.GONE);

				break;
			case AMOUNT:
				setFromWhoReview(View.VISIBLE);
				setAmountReview(View.GONE);
				setMessageReview(View.GONE);

				setRecipientInputViewStage(View.GONE);
				setAmountStage(View.VISIBLE);
				setMessageStage(View.GONE);

				previousInputStage = RequestStage.ENTER_RECIPIENT;
				nextInputStage = RequestStage.MESSAGE;

				sms_notice.setVisibility(View.GONE);
				break;
			case MESSAGE:
				setFromWhoReview(View.VISIBLE);
				setAmountReview(View.VISIBLE);
				setMessageReview(View.GONE);


				setRecipientInputViewStage(View.GONE);
				setAmountStage(View.GONE);
				setMessageStage(View.VISIBLE);

				previousInputStage = RequestStage.AMOUNT;
				nextInputStage = RequestStage.REVIEW;

				sms_notice.setVisibility(View.GONE);
				break;
			case REVIEW:
				setFromWhoReview(View.VISIBLE);
				setAmountReview(View.VISIBLE);
				setMessageReview(View.VISIBLE);


				setRecipientInputViewStage(View.GONE);
				setAmountStage(View.GONE);
				setMessageStage(View.GONE);

				previousInputStage = RequestStage.MESSAGE;
				nextInputStage = RequestStage.SEND;

				sms_notice.setVisibility(View.VISIBLE);
				break;
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


		if (requestCode == SEND_SMS && new PermissionHelper().permissionsGranted(grantResults)) {
			Amplitude.getInstance().logEvent(getString(R.string.sms_perm_success));
			sendRequest();
		} else {
			Amplitude.getInstance().logEvent(getString(R.string.sms_perm_denied));
			UIHelper.flashMessage(getContext(), getResources().getString(R.string.send_sms_perm_error));
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
