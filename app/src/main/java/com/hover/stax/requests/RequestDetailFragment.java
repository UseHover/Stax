package com.hover.stax.requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.hover.stax.R;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.databinding.FragmentRequestDetailBinding;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.Stax2LineItem;
import com.hover.stax.views.StaxDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestDetailFragment extends Fragment implements RequestSenderInterface {
    final public static String TAG = "RequestDetailFragment";

    private RequestDetailViewModel viewModel;
    private FragmentRequestDetailBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(RequestDetailViewModel.class);
        JSONObject data = new JSONObject();

        try {
            if (getArguments() != null) data.put("id", getArguments().getInt("id"));
        } catch (JSONException ignored) {
        }

        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_request_detail)), data, requireContext());

        binding = FragmentRequestDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.shareCard.requestLinkCardView.setTitle(getString(R.string.share_again_cardhead));

        viewModel.getRecipients().observe(getViewLifecycleOwner(), contacts -> {
            if (contacts != null && contacts.size() > 0) {
                for (StaxContact c : contacts)
                    createRecipientEntry(c);
            }
        });

        viewModel.getChannel().observe(getViewLifecycleOwner(), channel -> {
            binding.summaryCard.requesterAccountRow.setVisibility(channel != null ? View.VISIBLE : View.GONE);
            if (channel != null) {
                ((Stax2LineItem) view.findViewById(R.id.requesterValue)).setTitle(channel.name);
//				Timber.e("Activity is null? %s", (getActivity() == null));
            }
        });

        viewModel.getRequest().observe(getViewLifecycleOwner(), request -> {
            if (request != null) {
                setUpSummary(request);
            }
        });

        viewModel.setRequest(getArguments().getInt("id"));
        initShareButtons();
    }

    private void createRecipientEntry(StaxContact c) {
        Stax2LineItem ss2li = new Stax2LineItem(getContext(), null);
        ss2li.setContact(c);
        binding.summaryCard.requesteeValueList.addView(ss2li);
    }

    private void setUpSummary(Request request) {
        binding.summaryCard.requestMoneyCard.setTitle(request.description);
        binding.summaryCard.dateValue.setText(DateUtils.humanFriendlyDate(request.date_sent));

        if (request.amount != null && !request.amount.isEmpty()) {
            binding.summaryCard.amountRow.setVisibility(View.VISIBLE);
            binding.summaryCard.amountValue.setText(Utils.formatAmount(request.amount));
        } else
            binding.summaryCard.amountRow.setVisibility(View.GONE);

        if (request.requester_number != null && !request.requester_number.isEmpty())
            binding.summaryCard.requesterValue.setSubtitle(request.requester_number);


        binding.summaryCard.noteRow.setVisibility(request.note == null || request.note.isEmpty() ? View.GONE : View.VISIBLE);
        binding.summaryCard.noteValue.setText(request.note);

        binding.cancelBtn.setOnClickListener(btn -> showConfirmDialog());
    }

    private void showConfirmDialog() {
        if (getActivity() != null && getContext() != null) {
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

    public void initShareButtons() {
        if (getActivity() != null) {
            binding.shareCard.smsShareSelection.setOnClickListener(v -> sendSms(viewModel.getRequest().getValue(), viewModel.getRecipients().getValue(), getActivity()));
            binding.shareCard.whatsappShareSelection.setOnClickListener(v ->
                    sendWhatsapp(viewModel.getRequest().getValue(), viewModel.getRecipients().getValue(), viewModel.getChannel().getValue(), getActivity()));
            binding.shareCard.copylinkShareSelection.setOnClickListener(v -> copyShareLink(viewModel.getRequest().getValue(), binding.shareCard.copylinkShareSelection, getActivity()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }
}
