package com.hover.stax.home;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.utils.TimeAgo;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.List;

public class BalanceAdapter extends RecyclerView.Adapter<BalanceAdapter.BalanceViewHolder> {
	private List<BalanceModel> balanceModelList;

	public BalanceAdapter(List<BalanceModel> balanceModelList) {
		this.balanceModelList = balanceModelList;
	}

	@NonNull
	@Override
	public BalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.balance_items, parent, false);
		return new BalanceViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull BalanceViewHolder holder, int position) {
		BalanceModel balanceModel = balanceModelList.get(position);
		Channel channel = balanceModel.getChannel();

		holder.channelName.setText(balanceModel.getChannel().name);
		holder.amount.setText(Utils.formatAmount(balanceModel.getBalanceValue()));
		holder.timeAgo.setText(balanceModel.getTimeStamp() > 0 ? TimeAgo.timeAgo(ApplicationInstance.getContext(), balanceModel.getTimeStamp()) : "Refresh");
		holder.currency.setText(ApplicationInstance.getCurrency(channel.countryAlpha2));

		holder.balanced_swiped_layout.setBackgroundColor(Color.parseColor(balanceModel.getChannel().primaryColorHex));
		holder.currency.setTextColor(Color.parseColor(channel.secondaryColorHex));
		holder.timeAgo.setTextColor(Color.parseColor(channel.secondaryColorHex));
		holder.amount.setTextColor(Color.parseColor(channel.secondaryColorHex));
		UIHelper.setTextColoredDrawable(holder.timeAgo, R.drawable.ic_refresh_white_10dp, Color.parseColor(channel.secondaryColorHex));
	}

	@Override
	public int getItemCount() {
		return balanceModelList == null ? 0 : balanceModelList.size();
	}

	static class BalanceViewHolder extends RecyclerView.ViewHolder {
		private TextView channelName, timeAgo, currency, amount;
		private FrameLayout balanced_swiped_layout;

		public BalanceViewHolder(@NonNull View itemView) {
			super(itemView);
			channelName = itemView.findViewById(R.id.balance_channel);
			timeAgo = itemView.findViewById(R.id.balance_timeAgo);
			currency = itemView.findViewById(R.id.balance_currency);
			amount = itemView.findViewById(R.id.balance_amount);
			balanced_swiped_layout = itemView.findViewById(R.id.balanced_swiped_layout);
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
