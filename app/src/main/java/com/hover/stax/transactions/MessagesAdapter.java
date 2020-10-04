package com.hover.stax.transactions;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.TransactionMessageViewHolder> {

	private List<UssdCallResponse> messagesList;

	MessagesAdapter(List<UssdCallResponse> messagesList) {
		this.messagesList = messagesList;
	}

	@NonNull
	@Override
	public TransactionMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_messages_items, parent, false);
		return new TransactionMessageViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull TransactionMessageViewHolder holder, int position) {
		UssdCallResponse model = messagesList.get(position);
		if (!model.enteredValue.isEmpty())
			holder.enteredValueText.setText(model.enteredValue);
		else holder.enteredValueText.setVisibility(View.GONE);

		if (!model.responseMessage.isEmpty())
			holder.messageContentText.setText(model.responseMessage);
		else holder.messageContentText.setVisibility(View.GONE);
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
		TextView enteredValueText, messageContentText;

		TransactionMessageViewHolder(@NonNull View itemView) {
			super(itemView);
			enteredValueText = itemView.findViewById(R.id.message_enteredValue);
			messageContentText = itemView.findViewById(R.id.message_content);
		}
	}
}
