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
	private List<Channel> channels;

	private final RefreshListener refreshListener;

	public BalanceAdapter(List<Channel> channels, RefreshListener listener) {
		this.channels = channels;
		this.refreshListener = listener;
	}

	@NonNull
	@Override
	public BalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.balance_items, parent, false);
		return new BalanceViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull BalanceViewHolder holder, int position) {
		Channel channel = channels.get(position);

		holder.channelName.setText(channel.name);
		holder.channelId.setText(Integer.toString(channel.id));
		holder.amount.setText(Utils.formatAmount(channel.latestBalance));
		holder.timeAgo.setText(channel.latestBalanceTimestamp != null && channel.latestBalanceTimestamp > 0 ?
									   TimeAgo.timeAgo(ApplicationInstance.getContext(), channel.latestBalanceTimestamp) : "Refresh");
		holder.currency.setText(ApplicationInstance.getCurrency(channel.countryAlpha2));

		holder.balanced_swiped_layout.setBackgroundColor(Color.parseColor(channel.primaryColorHex));
		holder.currency.setTextColor(Color.parseColor(channel.secondaryColorHex));
		holder.timeAgo.setTextColor(Color.parseColor(channel.secondaryColorHex));
		holder.amount.setTextColor(Color.parseColor(channel.secondaryColorHex));
		UIHelper.setTextColoredDrawable(holder.timeAgo, R.drawable.ic_refresh_white_10dp, Color.parseColor(channel.secondaryColorHex));
	}

	class BalanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private TextView channelName, channelId, timeAgo, currency, amount;
		private FrameLayout balanced_swiped_layout;

		public BalanceViewHolder(@NonNull View itemView) {
			super(itemView);
			channelName = itemView.findViewById(R.id.balance_channel);
			channelId = itemView.findViewById(R.id.channel_id);
			timeAgo = itemView.findViewById(R.id.balance_timeAgo);
			timeAgo.setOnClickListener(this);
			currency = itemView.findViewById(R.id.balance_currency);
			amount = itemView.findViewById(R.id.balance_amount);
			balanced_swiped_layout = itemView.findViewById(R.id.balanced_swiped_layout);
		}

		@Override
		public void onClick(View v) {
			if (refreshListener != null)
				refreshListener.onTap(Integer.parseInt(channelId.getText().toString()));
		}
	}

	public interface RefreshListener {
		void onTap(int channelId);
	}

	@Override
	public int getItemCount() {
		return channels == null ? 0 : channels.size();
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
