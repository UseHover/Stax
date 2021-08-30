package com.hover.stax.transactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.databinding.TransactionMessagesItemsBinding;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.TransactionMessageViewHolder> {

    private final List<UssdCallResponse> messagesList;
    private  int forcedSize = 0;

    MessagesAdapter(List<UssdCallResponse> messagesList) {
        this.messagesList = messagesList;
    }

    MessagesAdapter(List<UssdCallResponse> messagesList, int forcedSize) {
        this.messagesList = messagesList;
        this.forcedSize = forcedSize;
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
        if (!model.enteredValue.isEmpty())
            holder.binding.messageEnteredValue.setText(model.enteredValue);
        else holder.binding.messageEnteredValue.setVisibility(View.GONE);

        if (!model.responseMessage.isEmpty())
            holder.binding.messageContent.setText(model.responseMessage);
        else holder.binding.messageContent.setVisibility(View.GONE);
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
        return (forcedSize > 0 && forcedSize <= messagesList.size()  ) ? forcedSize : messagesList.size();
    }

    static class TransactionMessageViewHolder extends RecyclerView.ViewHolder {
        TransactionMessagesItemsBinding binding;

        TransactionMessageViewHolder(TransactionMessagesItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
