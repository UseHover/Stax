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
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.List;

public class BalanceAdapter extends RecyclerView.Adapter<BalanceAdapter.BalanceViewHolder> {
	private List<Channel> channels;

	private final BalanceListener balanceListener;

	public BalanceAdapter(List<Channel> channels, BalanceListener listener) {
		this.channels = channels;
		this.balanceListener = listener;
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
		if (channel.latestBalance != null)
			holder.amount.setText(Utils.formatAmount(channel.latestBalance));
		holder.timeAgo.setText(channel.latestBalanceTimestamp != null && channel.latestBalanceTimestamp > 0 ?
									   DateUtils.timeAgo(ApplicationInstance.getContext(), channel.latestBalanceTimestamp) : "Refresh");
		holder.currency.setText(channel.currency);

		try {
			holder.balanced_swiped_layout.setBackgroundColor(Color.parseColor(channel.primaryColorHex));
		} catch (Exception ignored) {
		}
		try {
			holder.currency.setTextColor(Color.parseColor(channel.secondaryColorHex));
		} catch (Exception ignored) {
		}
		try {
			holder.timeAgo.setTextColor(Color.parseColor(channel.secondaryColorHex));
		} catch (Exception ignored) {
		}
		try {
			holder.amount.setTextColor(Color.parseColor(channel.secondaryColorHex));
		} catch (Exception ignored) {
		}
		try {
			UIHelper.setTextColoredDrawable(holder.timeAgo, R.drawable.ic_refresh_white_10dp, Color.parseColor(channel.secondaryColorHex));
		} catch (Exception e) {
		}
	}

	class BalanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private TextView channelName, channelId, timeAgo, currency, amount;
		private FrameLayout balanced_swiped_layout;

		public BalanceViewHolder(@NonNull View itemView) {
			super(itemView);
			channelName = itemView.findViewById(R.id.balance_channel);
			channelName.setOnClickListener(this);
			channelId = itemView.findViewById(R.id.channel_id);
			timeAgo = itemView.findViewById(R.id.balance_timeAgo);
			timeAgo.setOnClickListener(this);
			currency = itemView.findViewById(R.id.balance_currency);
			amount = itemView.findViewById(R.id.balance_amount);
			balanced_swiped_layout = itemView.findViewById(R.id.balanced_swiped_layout);
		}

		@Override
		public void onClick(View v) {
			if (balanceListener != null && v.getId() == R.id.balance_channel)
				balanceListener.onTapDetail(Integer.parseInt(channelId.getText().toString()));
			else if (balanceListener != null)
				balanceListener.onTapRefresh(Integer.parseInt(channelId.getText().toString()));
		}
	}

	public interface BalanceListener {
		void onTapRefresh(int channelId);

		void onTapDetail(int channelId);
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
