package com.hover.stax.balances;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.databinding.HomeListItemBinding;
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
		HomeListItemBinding binding = HomeListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		return new ScheduledAdapter.ScheduledViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ScheduledAdapter.ScheduledViewHolder holder, int position) {
		Schedule s = scheduleList.get(position);
		holder.binding.liDescription.setText(String.format("%s%s", s.description.substring(0, 1).toUpperCase(), s.description.substring(1)));
		holder.binding.liAmount.setText(s.amount != null ? Utils.formatAmount(s.amount) : "none");
		holder.binding.liHeader.setVisibility(shouldShowDate(s, position, holder.itemView.getContext()) ? View.VISIBLE : View.GONE);
		holder.binding.liHeader.setText(s.humanFrequency(holder.itemView.getContext()));
		holder.itemView.setOnClickListener(view -> selectListener.viewScheduledDetail(s.id));
	}

	private boolean shouldShowDate(Schedule s, int position, Context c) {
		return position == 0 || !scheduleList.get(position - 1).humanFrequency(c).equals(s.humanFrequency(c));
	}

	@Override
	public int getItemCount() {
		return scheduleList != null ? scheduleList.size() : 0;
	}

	static class ScheduledViewHolder extends RecyclerView.ViewHolder {
		public HomeListItemBinding binding;

		ScheduledViewHolder(HomeListItemBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
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
