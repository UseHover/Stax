package com.hover.stax.home;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.List;

import static android.view.View.GONE;

public class BalanceAdapter extends RecyclerView.Adapter<BalanceAdapter.BalanceViewHolder> {
	private List<Channel> channels;

	private final BalanceListener balanceListener;
	private boolean showBalance = false;

	public BalanceAdapter(List<Channel> channels, BalanceListener listener) {
		this.channels = channels;
		this.balanceListener = listener;
	}
	public void updateShowBalance() {
		this.showBalance = !showBalance;
		this.notifyDataSetChanged();
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


		if(!showBalance)holder.subtitle.setVisibility(GONE);
		if (channel.latestBalance != null && showBalance) {
			holder.subtitle.setVisibility(View.VISIBLE);
			holder.subtitle.setText(DateUtils.humanFriendlyDate(channel.latestBalanceTimestamp));
			holder.amount.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
			holder.amount.setText(Utils.formatAmount(channel.latestBalance));
		}
		else {
			holder.amount.setText("");
			holder.amount.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_remove_24,0);
		}
		if(showBalance && channel.latestBalance == null) {
			holder.subtitle.setVisibility(View.VISIBLE);
			holder.subtitle.setText(holder.itemView.getContext().getString(R.string.refresh_balance_desc));
		}

		setColors(holder, channel,
			UIHelper.getColor(channel.primaryColorHex, true, holder.itemView.getContext()),
			UIHelper.getColor(channel.secondaryColorHex, false, holder.itemView.getContext()));
	}

	private void setColors(BalanceViewHolder holder, Channel channel, int primary, int secondary) {
		holder.itemView.setBackgroundColor(primary);
		holder.subtitle.setTextColor(secondary);
		holder.amount.setTextColor(secondary);
		holder.channelName.setTextColor(secondary);

		Drawable drawable = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_refresh_white_13dp);
		if(drawable !=null) {
			drawable = DrawableCompat.wrap(drawable);
			DrawableCompat.setTint(drawable.mutate(), secondary);
			holder.subtitle.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
		}
	}

	class BalanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private TextView channelName, channelId, subtitle, amount;

		public BalanceViewHolder(@NonNull View itemView) {
			super(itemView);
			channelName = itemView.findViewById(R.id.balance_channel);
			channelName.setOnClickListener(this);
			channelId = itemView.findViewById(R.id.channel_id);
			amount = itemView.findViewById(R.id.balance_amount);
			subtitle = itemView.findViewById(R.id.balance_subtitle);
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
		void triggerRefreshAll();
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
