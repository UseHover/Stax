package com.hover.stax.transactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;

import java.util.List;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.HistoryViewHolder> {
	private List<StaxTransaction> transactionList;
	private final SelectListener selectListener;
	private int mainChannelId = -1;

	public TransactionHistoryAdapter(List<StaxTransaction> transactions, SelectListener selectListener) {
		this.transactionList = transactions;
		this.selectListener = selectListener;
	}

	public TransactionHistoryAdapter(List<StaxTransaction> transactions, SelectListener selectListener, int mainChannelId) {
		this.transactionList = transactions;
		this.selectListener = selectListener;
		this.mainChannelId = mainChannelId;
	}
	@NonNull
	@Override
	public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_list_item, parent, false);
		return new HistoryViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
		StaxTransaction t = transactionList.get(position);
		TransactionDescriptionClickListener transactionDescriptionClickListener = (clickType) -> {
			if(clickType == ClickType.CHANNEL) selectListener.onTapChannel(t.channel_id);
			else selectListener.onTap(t.uuid);
		};

		if(mainChannelId != -1 && mainChannelId == t.channel_id)
			holder.content.setText(t.description);
		else
			UIHelper.makeChannelNameALink(t.description, holder.content, t.fromInstitutionStartPos, t.fromInstitutionEndPos, transactionDescriptionClickListener);


		holder.amount.setText(t.amount);
		holder.date.setVisibility(shouldShowDate(t, position) ? View.VISIBLE : View.GONE);
		holder.date.setText(DateUtils.humanFriendlyDate(t.initiated_at));
		holder.itemView.setOnClickListener(view -> {
			selectListener.onTap(t.uuid);
		});
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
		private TextView content, amount, date;

		HistoryViewHolder(@NonNull View itemView) {
			super(itemView);
			content = itemView.findViewById(R.id.trans_content);
			amount = itemView.findViewById(R.id.trans_amount);
			date = itemView.findViewById(R.id.trans_date);
		}

	}

	public interface SelectListener {
		void onTap(String uuid);
		void onTapChannel(int channelId);
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
