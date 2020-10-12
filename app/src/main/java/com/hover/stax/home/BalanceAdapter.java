package com.hover.stax.home;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.utils.bubbleshowcase.BubbleShowCase;
import com.hover.stax.utils.bubbleshowcase.BubbleShowCaseListener;

import java.util.List;

public class BalanceAdapter extends RecyclerView.Adapter<BalanceAdapter.BalanceViewHolder> {
	private List<Channel> channels;

	private final BalanceListener balanceListener;
	private Activity activity;
	private BubbleShowCaseListener balanceShowcaseListener;

	void balanceShowcase(BubbleShowCaseListener balanceShowcaseListener, Activity activity) {
		this.balanceShowcaseListener = balanceShowcaseListener;
		this.activity = activity;
		this.notifyDataSetChanged();
	}

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
		holder.balanced_swiped_layout.setBackgroundColor(Color.parseColor(channel.primaryColorHex));
		//holder.refreshButton.setImageTintList(Color.parseColor(channel.secondaryColorHex));
		holder.amount.setTextColor(Color.parseColor(channel.secondaryColorHex));
		UIHelper.setColoredDrawable(holder.refreshButton, R.drawable.ic_refresh_white_24dp, Color.parseColor(channel.secondaryColorHex));

		if (position == 0 && activity != null && balanceShowcaseListener != null) {
			BubbleShowCase.Companion.showCase(
					Utils.getStaxString(R.string.keep_accounts_private),
					Utils.getStaxString(R.string.keep_accounts_private_desc),
					BubbleShowCase.ArrowPosition.TOP,
					balanceShowcaseListener,
					holder.dragIcon,
					activity);
		}
	}

	class BalanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private TextView channelName, channelId, amount;
		private ImageButton refreshButton;
		private FrameLayout balanced_swiped_layout;
		private ImageView dragIcon;

		public BalanceViewHolder(@NonNull View itemView) {
			super(itemView);
			channelName = itemView.findViewById(R.id.balance_channel);
			channelName.setOnClickListener(this);
			channelId = itemView.findViewById(R.id.channel_id);
			refreshButton = itemView.findViewById(R.id.refresh_button);
			refreshButton.setOnClickListener(this);
			amount = itemView.findViewById(R.id.balance_amount);
			balanced_swiped_layout = itemView.findViewById(R.id.balanced_swiped_layout);
			dragIcon = itemView.findViewById(R.id.balance_drag);
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
