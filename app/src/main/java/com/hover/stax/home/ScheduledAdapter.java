package com.hover.stax.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.Utils;

import java.util.List;

public class ScheduledAdapter extends RecyclerView.Adapter<ScheduledAdapter.ScheduledViewHolder> {
	private List<Schedule> scheduleList;
	private final SelectListener selectListener;

	public ScheduledAdapter(List<Schedule> scheduled, SelectListener selectListener) {
		this.scheduleList = scheduled;
		this.selectListener = selectListener;
	}

	@NonNull
	@Override
	public ScheduledAdapter.ScheduledViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_list_item, parent, false);
		return new ScheduledAdapter.ScheduledViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ScheduledAdapter.ScheduledViewHolder holder, int position) {
		Schedule s = scheduleList.get(position);
		holder.description.setText(s.description.substring(0, 1).toUpperCase() + s.description.substring(1));
		holder.amount.setText(s.amount != null ? Utils.formatAmount(s.amount) : "none");
		holder.header.setVisibility(shouldShowDate(s, position, holder.itemView.getContext()) ? View.VISIBLE : View.GONE);
		holder.header.setText(s.humanFrequency(holder.itemView.getContext()));
		holder.itemView.setOnClickListener(view -> {
			selectListener.viewScheduledDetail(s.id);
		});
	}

	private boolean shouldShowDate(Schedule s, int position, Context c) {
		return position == 0 || !scheduleList.get(position - 1).humanFrequency(c).equals(s.humanFrequency(c));
	}

	@Override
	public int getItemCount() {
		return scheduleList != null ? scheduleList.size() : 0;
	}

	static class ScheduledViewHolder extends RecyclerView.ViewHolder {
		private TextView description, amount, header;

		ScheduledViewHolder(@NonNull View itemView) {
			super(itemView);
			description = itemView.findViewById(R.id.trans_content);
			amount = itemView.findViewById(R.id.trans_amount);
			header = itemView.findViewById(R.id.trans_date);
		}
	}

	public interface SelectListener {
		void viewScheduledDetail(int id);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}
}
