package com.hover.stax.futureTransactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.databinding.TransactionListItemBinding;
import com.hover.stax.requests.Request;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.Utils;

import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestsViewHolder> {

    private List<Request> requestList;
    private final SelectListener selectListener;

    public RequestsAdapter(List<Request> requests, SelectListener selectListener) {
        this.requestList = requests;
        this.selectListener = selectListener;
    }
    public void updateData(List<Request> requests) {
        this.requestList = requests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestsAdapter.RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TransactionListItemBinding binding = TransactionListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RequestsAdapter.RequestsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestsAdapter.RequestsViewHolder holder, int position) {
        Request r = requestList.get(position);

        holder.binding.liDescription.setText(r.description);
        holder.binding.liAmount.setText(r.amount != null ? Utils.formatAmount(r.amount) : "none");
        holder.binding.liHeader.setVisibility(shouldShowDate(r, position) ? View.VISIBLE : View.GONE);
        holder.binding.liHeader.setText(DateUtils.humanFriendlyDate(r.date_sent));

        holder.itemView.setOnClickListener(view -> selectListener.viewRequestDetail(r.id));
    }

    private boolean shouldShowDate(Request r, int position) {
        return position == 0 || !DateUtils.humanFriendlyDate(requestList.get(position - 1).date_sent)
                .equals(DateUtils.humanFriendlyDate(r.date_sent));
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    static class RequestsViewHolder extends RecyclerView.ViewHolder {
        public TransactionListItemBinding binding;

        RequestsViewHolder(TransactionListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface SelectListener {
        void viewRequestDetail(int id);
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