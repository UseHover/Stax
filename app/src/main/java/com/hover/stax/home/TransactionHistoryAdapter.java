package com.hover.stax.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.utils.interfaces.CustomOnClickListener;

import java.util.List;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.HistoryViewHolder> {
	private List<StaxTransactionModel> transactionModelList;

	TransactionHistoryAdapter(List<StaxTransactionModel> transactionModelList) {
		this.transactionModelList = transactionModelList;
	}

	@NonNull
	@Override
	public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_list_item, parent, false);
		return new HistoryViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
	StaxTransactionModel model = transactionModelList.get(position);
	holder.content.setText("From "+model.getChannelName()+" to "+model.getToTransactionType());
	holder.amount.setText(model.getAmount());
	holder.date.setVisibility(model.isShowDate() ? View.VISIBLE : View.GONE);
	holder.date.setText(model.getStaxDate().getMonth()+" "+model.getStaxDate().getDayOfMonth());
	}

	@Override
	public int getItemCount() {
		return transactionModelList !=null ? transactionModelList.size() : 0;
	}

	static class HistoryViewHolder extends RecyclerView.ViewHolder {
		private TextView  content, amount, date;
		HistoryViewHolder(@NonNull View itemView) {
			super(itemView);
			content = itemView.findViewById(R.id.trans_content);
			amount = itemView.findViewById(R.id.trans_amount);
			date = itemView.findViewById(R.id.trans_date);
		}
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
