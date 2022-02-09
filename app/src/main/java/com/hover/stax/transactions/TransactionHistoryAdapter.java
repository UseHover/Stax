package com.hover.stax.transactions;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.transactions.Transaction;
import com.hover.stax.databinding.TransactionListItemBinding;
import com.hover.stax.utils.DateUtils;

import java.util.List;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.HistoryViewHolder> {

    private final SelectListener selectListener;
    private List<StaxTransaction> transactions;
    private List<HoverAction> actions;

    public TransactionHistoryAdapter(List<StaxTransaction> transactions, List<HoverAction> actions, SelectListener selectListener) {
        this.transactions = transactions;
        this.actions = actions;
        this.selectListener = selectListener;
    }

    public void updateData(List<StaxTransaction> ts, List<HoverAction> as) {
        if (ts == null || as == null) return;
        transactions = ts;
        actions = as;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TransactionListItemBinding binding = TransactionListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        StaxTransaction t = transactions.get(position);

        holder.binding.liDescription.setText(String.format("%s%s", t.description.substring(0, 1).toUpperCase(), t.description.substring(1)));
        holder.binding.liAmount.setText(t.getDisplayAmount());
        holder.binding.liHeader.setVisibility(shouldShowDate(t, position) ? View.VISIBLE : View.GONE);
        holder.binding.liHeader.setText(DateUtils.humanFriendlyDate(t.initiated_at));

        holder.itemView.setOnClickListener(view -> selectListener.viewTransactionDetail(t.uuid));
        setStatus(t, holder);
    }

    private void setStatus(StaxTransaction t, HistoryViewHolder holder) {
        TransactionStatus ts = new TransactionStatus(t);
        HoverAction a = findAction(t.action_id);
        holder.binding.liAmount.setAlpha((float) (t.status.equals(Transaction.FAILED) ? 0.54 : 1.0));
        holder.binding.transactionItemLayout.setBackgroundColor(holder.binding.getRoot().getContext().getResources().getColor(ts.getBackgroundColor()));
        holder.binding.liStatus.setText(HtmlCompat.fromHtml(ts.getShortStatusDetail(a, holder.binding.getRoot().getContext()), HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.binding.liStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(ts.getIcon(), 0, 0, 0);
    }

    private HoverAction findAction(String public_id) {
        for (HoverAction a : actions) {
            if (a.public_id.equals(public_id))
                return a;
        }
        return null;
    }

    private boolean shouldShowDate(StaxTransaction t, int position) {
        return position == 0 ||
                !DateUtils.humanFriendlyDate(transactions.get(position - 1).initiated_at)
                        .equals(DateUtils.humanFriendlyDate(t.initiated_at));
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface SelectListener {
        void viewTransactionDetail(String uuid);
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        public TransactionListItemBinding binding;

        HistoryViewHolder(TransactionListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
