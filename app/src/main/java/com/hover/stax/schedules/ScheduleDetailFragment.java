package com.hover.stax.schedules;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ScheduleDetailFragment extends Fragment {
	final public static String TAG = "ScheduleFragment";

	private ScheduleDetailViewModel viewModel;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewModel = new ViewModelProvider(this).get(ScheduleDetailViewModel.class);
		JSONObject data = new JSONObject();
		try { data.put("id", getArguments().getInt("id"));
		} catch (JSONException e) { }
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_schedule)), data);
		return inflater.inflate(R.layout.fragment_schedule, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		viewModel.getSchedule().observe(getViewLifecycleOwner(), schedule -> {
			if (schedule != null) {
				((TextView) view.findViewById(R.id.title)).setText(schedule.description);
				((TextView) view.findViewById(R.id.details_amount)).setText(Utils.formatAmount(schedule.amount));
				((TextView) view.findViewById(R.id.details_date)).setText(DateUtils.humanFriendlyDate(schedule.start_date));

				view.findViewById(R.id.frequencyRow).setVisibility(schedule.frequency.equals(Schedule.ONCE) ? View.GONE : View.VISIBLE);
				((TextView) view.findViewById(R.id.details_frequency)).setText(schedule.humanFrequency(getContext()));

				view.findViewById(R.id.endRow).setVisibility(schedule.frequency.equals(Schedule.ONCE) ? View.GONE : View.VISIBLE);
				((TextView) view.findViewById(R.id.details_end)).setText(DateUtils.humanFriendlyDate(schedule.end_date));

				view.findViewById(R.id.reasonRow).setVisibility(schedule.reason == null || schedule.reason.isEmpty() ? View.GONE : View.VISIBLE);
				((TextView) view.findViewById(R.id.details_reason)).setText(schedule.reason);
			}
		});

		viewModel.setSchedule(getArguments().getInt("id"));
	}
}
