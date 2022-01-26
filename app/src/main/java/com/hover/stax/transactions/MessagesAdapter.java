package com.hover.stax.transactions;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.databinding.TransactionMessagesItemsBinding;
import com.hover.stax.utils.Utils;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.TransactionMessageViewHolder> {

    private final List<UssdCallResponse> messagesList;

    MessagesAdapter(List<UssdCallResponse> messagesList) {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public TransactionMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TransactionMessagesItemsBinding binding = TransactionMessagesItemsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TransactionMessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionMessageViewHolder holder, int position) {
        UssdCallResponse model = messagesList.get(position);
        if (!model.enteredValue.isEmpty()) {
            holder.binding.messageEnteredValue.setText(model.enteredValue);
            if (model.isShortCode)
                styleAsLink(holder.binding.messageEnteredValue, model.enteredValue);
        } else holder.binding.messageEnteredValue.setVisibility(View.GONE);

        if (!model.responseMessage.isEmpty())
            holder.binding.messageContent.setText(model.responseMessage);
        else holder.binding.messageContent.setVisibility(View.GONE);
    }

    private void styleAsLink(TextView tv, String shortcode) {
        tv.setTextColor(tv.getContext().getResources().getColor(R.color.brightBlue));
        tv.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        tv.setOnClickListener(v -> Utils.dial(shortcode, v.getContext()));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (messagesList == null) return 0;
        return messagesList.size();
    }

    static class TransactionMessageViewHolder extends RecyclerView.ViewHolder {
        TransactionMessagesItemsBinding binding;

        TransactionMessageViewHolder(TransactionMessagesItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
