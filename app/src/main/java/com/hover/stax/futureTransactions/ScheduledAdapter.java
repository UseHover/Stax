package com.hover.stax.futureTransactions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.databinding.TransactionListItemBinding;
import com.hover.stax.database.models.Schedule;
import com.hover.stax.core.Utils;

import java.util.List;

public class ScheduledAdapter extends RecyclerView.Adapter<ScheduledAdapter.ScheduledViewHolder> {
    private final SelectListener selectListener;
    private List<Schedule> scheduleList;

    public ScheduledAdapter(List<Schedule> scheduled, SelectListener selectListener) {
        this.scheduleList = scheduled;
        this.selectListener = selectListener;
    }

    public void updateData(List<Schedule> scheduled) {
        this.scheduleList = scheduled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScheduledAdapter.ScheduledViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TransactionListItemBinding binding = TransactionListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ScheduledAdapter.ScheduledViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduledAdapter.ScheduledViewHolder holder, int position) {
        Schedule s = scheduleList.get(position);
        holder.binding.liTitle.setText(String.format("%s%s", s.description.substring(0, 1).toUpperCase(), s.description.substring(1)));
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public interface SelectListener {
        void viewScheduledDetail(int id);
    }

    static class ScheduledViewHolder extends RecyclerView.ViewHolder {
        public TransactionListItemBinding binding;

        ScheduledViewHolder(TransactionListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
