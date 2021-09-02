package com.hover.stax.transactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.sdk.transactions.Transaction;
import com.hover.stax.R;
import com.hover.stax.databinding.HomeListItemBinding;
import com.hover.stax.utils.DateUtils;

import java.util.List;

import timber.log.Timber;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.HistoryViewHolder> {

    private final List<StaxTransaction> transactionList;
    private final SelectListener selectListener;

    public TransactionHistoryAdapter(List<StaxTransaction> transactions, SelectListener selectListener) {
        this.transactionList = transactions;
        this.selectListener = selectListener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HomeListItemBinding binding = HomeListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        StaxTransaction t = transactionList.get(position);

        setBGAndCallout(t, holder);

        holder.binding.liDescription.setText(String.format("%s%s", t.description.substring(0, 1).toUpperCase(), t.description.substring(1)));
        holder.binding.liAmount.setText(t.getDisplayAmount());
        holder.binding.liHeader.setVisibility(shouldShowDate(t, position) ? View.VISIBLE : View.GONE);
        holder.binding.liHeader.setText(DateUtils.humanFriendlyDate(t.initiated_at));

        holder.itemView.setOnClickListener(view -> selectListener.viewTransactionDetail(t.uuid));
    }

    private void setBGAndCallout(StaxTransaction t, HistoryViewHolder holder) {
        if (t.status.equals(Transaction.PENDING)) {
            holder.binding.transactionItemLayout.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.cardDarkBlue));
            holder.binding.liCallout.setVisibility(View.VISIBLE);
        }
        else if(t.status.equals(Transaction.FAILED)) {
            holder.binding.transactionItemLayout.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.cardDarkRed));
            holder.binding.liCallout.setText(t.getFullStatus().getDetail());
            holder.binding.liCallout.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_info_red, 0, 0, 0);
            holder.binding.liCallout.setVisibility(View.VISIBLE);
        }
        else {
            holder.binding.transactionItemLayout.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
            holder.binding.liCallout.setVisibility(View.GONE);
        }
    }


    private boolean shouldShowDate(StaxTransaction t, int position) {
        return position == 0 ||
                !DateUtils.humanFriendlyDate(transactionList.get(position - 1).initiated_at)
                        .equals(DateUtils.humanFriendlyDate(t.initiated_at));
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        public HomeListItemBinding binding;

        HistoryViewHolder(HomeListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface SelectListener {
        void viewTransactionDetail(String uuid);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
