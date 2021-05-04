package com.hover.stax.schedules;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.databinding.FragmentScheduleBinding;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.Stax2LineItem;
import com.hover.stax.views.StaxDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class ScheduleDetailFragment extends Fragment {
	final public static String TAG = "ScheduleFragment";

	private ScheduleDetailViewModel viewModel;

	private FragmentScheduleBinding binding;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewModel = new ViewModelProvider(this).get(ScheduleDetailViewModel.class);
		JSONObject data = new JSONObject();
		try {
			data.put("id", getArguments().getInt("id"));
		} catch (JSONException ignored) {
		}
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_schedule)), data);

		binding = FragmentScheduleBinding.inflate(inflater, container, false);

		return binding.getRoot();
//				inflater.inflate(R.layout.fragment_schedule, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		viewModel.getSchedule().observe(getViewLifecycleOwner(), schedule -> {
			if (schedule != null) {
				setUpSummary(schedule);
				setUpTestBtn(schedule);
			}
		});

		viewModel.getContacts().observe(getViewLifecycleOwner(), contacts -> {
			if (contacts != null && contacts.size() > 0) {
				for (StaxContact c: contacts)
					createRecipientEntry(c);
			}
		});

		viewModel.setSchedule(getArguments().getInt("id"));
	}

	private void setUpSummary(Schedule schedule) {
		binding.scheduleDetailsCard.setTitle(schedule.description);

		binding.summaryCard.detailsAmount.setText(Utils.formatAmount(schedule.amount));
		binding.summaryCard.detailsDate.setText(DateUtils.humanFriendlyDate(schedule.start_date));

		binding.summaryCard.frequencyRow.setVisibility(schedule.frequency == Schedule.ONCE ? View.GONE : View.VISIBLE);
		binding.summaryCard.detailsFrequency.setText(schedule.humanFrequency(getContext()));

		binding.summaryCard.endRow.setVisibility(schedule.frequency == Schedule.ONCE || schedule.end_date == null ? View.GONE : View.VISIBLE);
		binding.summaryCard.detailsEnd.setText(schedule.end_date != null ? DateUtils.humanFriendlyDate(schedule.end_date) : "");

		binding.summaryCard.noteRow.setVisibility(schedule.note == null || schedule.note.isEmpty() ? View.GONE : View.VISIBLE);
		binding.summaryCard.detailsReason.setText(schedule.note);

		binding.cancelBtn.setOnClickListener(this::showConfirmDialog);
	}

	private void createRecipientEntry(StaxContact c) {
		Stax2LineItem ss2li = new Stax2LineItem(getContext(), null);
		ss2li.setContact(c);
		binding.summaryCard.requesteeValueList.addView(ss2li);
	}

	private void showConfirmDialog(View v) {
		new StaxDialog(requireActivity())
				.setDialogTitle(R.string.cancelfuture_head)
				.setDialogMessage(R.string.cancelfuture_msg)
				.setNegButton(R.string.btn_back, btn -> {
				})
				.setPosButton(R.string.btn_canceltrans, btn -> {
					viewModel.deleteSchedule();
					UIHelper.flashMessage(requireActivity(), getString(R.string.toast_confirm_cancelfuture));
					NavHostFragment.findNavController(ScheduleDetailFragment.this).popBackStack();
				})
				.isDestructive()
				.showIt();
	}

	private void setUpTestBtn(Schedule schedule) {
		binding.testBtn.setVisibility(Utils.usingDebugVariant(getContext()) ? View.VISIBLE : View.GONE);
		binding.testBtn.setOnClickListener(btn -> {
			WorkManager.getInstance(getContext())
					.beginUniqueWork("TEST", ExistingWorkPolicy.REPLACE, ScheduleWorker.makeWork()).enqueue();
			if (!schedule.isScheduledForToday())
				UIHelper.flashMessage(getContext(), "Shouldn't show notification, not scheduled for today.");
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		binding = null;
	}
}
