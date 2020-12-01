package com.hover.stax.requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.StaxDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class RequestDetailFragment extends Fragment {
	final public static String TAG = "RequestDetailFragment";

	private RequestDetailViewModel viewModel;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewModel = new ViewModelProvider(this).get(RequestDetailViewModel.class);
		JSONObject data = new JSONObject();

		try { if(getArguments() !=null) data.put("id", getArguments().getInt("id")); }
		catch (JSONException ignored) { }

		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_request_detail)), data);
		return inflater.inflate(R.layout.fragment_request_detail, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		viewModel.getRecipients().observe(getViewLifecycleOwner(), contacts -> {
			if (contacts != null && contacts.size() > 0) {
				for (StaxContact c : contacts)
					createRecipientEntry(c, view);
			}
		});

		viewModel.getRequest().observe(getViewLifecycleOwner(), request -> {
			if (request != null) {
				setUpSummary(view, request);
			}
		});

		viewModel.setRequest(getArguments().getInt("id"));
		initShareButtons(view);
	}

	private void createRecipientEntry(StaxContact c, View view) {
		View v = LayoutInflater.from(getContext()).inflate(R.layout.recipient_cell, null);
		TextView name = v.findViewById(R.id.recipient_name);
		TextView phone = v.findViewById(R.id.recipient_phone);

		if (c.shortName().equals(c.phoneNumber)) {
			name.setText(c.shortName());
			phone.setVisibility(View.GONE);
		} else {
			name.setText(c.shortName());
			phone.setText(c.phoneNumber);
		}
		((LinearLayout) view.findViewById(R.id.recipientValueList)).addView(v);
	}

	private void setUpSummary(View view, Request request) {
		((TextView) view.findViewById(R.id.title)).setText(request.description);
		((TextView) view.findViewById(R.id.dateValue)).setText(DateUtils.humanFriendlyDate(request.date_sent));

		if (request.amount != null && !request.amount.isEmpty()) {
			view.findViewById(R.id.amountRow).setVisibility(View.VISIBLE);
			((TextView) view.findViewById(R.id.amountValue)).setText(Utils.formatAmount(request.amount));
		} else
			view.findViewById(R.id.amountRow).setVisibility(View.GONE);

		if (request.requester_number != null && !request.requester_number.isEmpty()) {
			view.findViewById(R.id.requesteeChannelRow).setVisibility(View.VISIBLE);
			//((TextView) view.findViewById(R.id.requesteeChannelValue)).setText(request.);
			((TextView) view.findViewById(R.id.requesteeNumberValue)).setText(request.requester_number);
		}


		view.findViewById(R.id.noteRow).setVisibility(request.note == null || request.note.isEmpty() ? View.GONE : View.VISIBLE);
		((TextView) view.findViewById(R.id.noteValue)).setText(request.note);

		view.findViewById(R.id.cancel_btn).setOnClickListener(btn -> showConfirmDialog());
	}

	private void showConfirmDialog() {
		if(getActivity() !=null && getContext() !=null) {
			new StaxDialog(getActivity())
					.setDialogTitle(R.string.cancelreq_head)
					.setDialogMessage(R.string.cancelreq_msg)
					.setNegButton(R.string.btn_back, btn -> {
					})
					.setPosButton(R.string.btn_cancelreq, btn -> {
						viewModel.deleteRequest();
						UIHelper.flashMessage(getContext(), getString(R.string.toast_confirm_cancelreq));
						NavHostFragment.findNavController(RequestDetailFragment.this).popBackStack();
					})
					.isDestructive()
					.showIt();
		}
	}


	public void initShareButtons(View view) {
		if(getContext() !=null && getActivity() !=null) {
			view.findViewById(R.id.sms_share_selection).setOnClickListener(v -> Request.sendUsingSms(viewModel.generateRecipientString(), viewModel.generateSMS(), getContext(), getActivity()));
			view.findViewById(R.id.whatsapp_share_selection).setOnClickListener(v -> viewModel.getCountryAlphaAndSendWithWhatsApp(getContext(), getActivity()));
			view.findViewById(R.id.copylink_share_selection).setOnClickListener(v -> {
				ImageView copyImage = v.findViewById(R.id.copyLinkImage);
				if (Utils.copyToClipboard(viewModel.generateSMS(), getActivity())) {
					copyImage.setActivated(true);
					copyImage.setImageResource(R.drawable.copy_icon_white);

					TextView copyLabel = v.findViewById(R.id.copyLinkText);
					copyLabel.setText(getString(R.string.link_copied_label));
				} else {
					copyImage.setActivated(false);
				}
			});
		}
	}

}
