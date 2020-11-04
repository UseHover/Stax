package com.hover.stax.home;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
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
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.balance_item, parent, false);
		return new BalanceViewHolder(view);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void onBindViewHolder(@NonNull BalanceViewHolder holder, int position) {
		Channel channel = channels.get(position);

		holder.channelName.setText(channel.name);
		holder.channelId.setText(Integer.toString(channel.id));
		if (channel.latestBalance != null)
			holder.amount.setText(Utils.formatAmount(channel.latestBalance));
		setColors(holder, channel,
			UIHelper.getColor(channel.primaryColorHex, true, holder.itemView.getContext()),
			UIHelper.getColor(channel.secondaryColorHex, false, holder.itemView.getContext()));
		if (channel.latestBalanceTimestamp != null && channel.latestBalanceTimestamp > (DateUtils.now() - DateUtils.MIN))
			holder.swl.open(true);
	}

	private void setColors(BalanceViewHolder holder, Channel channel, int primary, int secondary) {
		holder.amount.setTextColor(secondary);
		UIHelper.setColoredDrawable(holder.refreshButton, R.drawable.ic_refresh_white_24dp, secondary);
		holder.balanced_swiped_layout.setBackgroundColor(primary);
		if (Build.VERSION.SDK_INT >= 21) {
			RippleDrawable rippleDrawable = (RippleDrawable) holder.refreshButton.getBackground(); // assumes bg is a RippleDrawable

			int[][] states = new int[][]{new int[]{android.R.attr.state_enabled}};
			int[] colors = new int[]{ secondary };

			ColorStateList colorStateList = new ColorStateList(states, colors);
			rippleDrawable.setColor(colorStateList);
		}
	}

	class BalanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private TextView channelName, channelId, amount;
		private ImageButton refreshButton;
		private FrameLayout balanced_swiped_layout;
		private SwipeRevealLayout swl;

		public BalanceViewHolder(@NonNull View itemView) {
			super(itemView);
			channelName = itemView.findViewById(R.id.balance_channel);
			channelName.setOnClickListener(this);
			channelId = itemView.findViewById(R.id.channel_id);
			refreshButton = itemView.findViewById(R.id.refresh_button);
			refreshButton.setOnClickListener(this);
			amount = itemView.findViewById(R.id.balance_amount);
			balanced_swiped_layout = itemView.findViewById(R.id.balanced_swiped_layout);
			swl = itemView.findViewById(R.id.swipe_reveal_layout);
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
