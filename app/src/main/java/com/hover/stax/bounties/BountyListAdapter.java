package com.hover.stax.bounties;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.transactions.StaxTransaction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class BountyListAdapter extends RecyclerView.Adapter<BountyListAdapter.BountyListViewHolder> {
	private static final String TAG = "BountyListAdapter";

	private List<Bounty> bountyList;
	private final SelectListener selectListener;

	BountyListAdapter(List<Bounty> bounties, SelectListener listener) {
		bountyList = bounties;
		selectListener = listener;
	}

	@NonNull
	@Override
	public BountyListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_list_item, parent, false);
		return new BountyListViewHolder(view);
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onBindViewHolder(@NonNull BountyListViewHolder holder, int position) {
		HoverAction a = bountyList.get(position).action;
		chooseState(holder, bountyList.get(position));
		holder.setDescription(a);
		holder.amount.setText(holder.itemView.getContext().getString(R.string.bounty_amount_with_currency, a.bounty_amount));
	}

	private void chooseState(BountyListViewHolder holder, Bounty bounty) {
		if (!bounty.action.bounty_is_open && bounty.transactionCount() > 0) { // Bounty is closed and done by current user
			holder.setState(R.color.muted_green, R.string.done, R.drawable.ic_check, false,null);
		} else if (!bounty.action.bounty_is_open) { // This bounty is closed and done by another user
			holder.setState(R.color.lighter_grey, 0, 0, false,null);
		} else if (bounty.transactionCount() > 0) { // Bounty is open and with a transaction by current user
			holder.setState(R.color.pending_brown, R.string.bounty_pending_short_desc, R.drawable.ic_warning, true,
				(view) -> selectListener.viewTransactionDetail(bounty.transactions.get(0).uuid));
		} else
			holder.setState(R.color.cardViewColor, 0, 0, true, (view) -> selectListener.bountyDetail(bounty));
	}

	@Override
	public int getItemCount() {
		return bountyList != null ? bountyList.size() : 0;
	}

	static class BountyListViewHolder extends RecyclerView.ViewHolder {
		private TextView content, amount, notice;
		private LinearLayout parentLayout;

		public BountyListViewHolder(@NonNull View itemView) {
			super(itemView);
			parentLayout = itemView.findViewById(R.id.transaction_item_layout);
			content = itemView.findViewById(R.id.li_description);
			amount = itemView.findViewById(R.id.li_amount);
			notice = itemView.findViewById(R.id.li_callout);
			TextView date = itemView.findViewById(R.id.li_header);
			date.setVisibility(View.GONE);
		}

		private void setState(int color, int noticeString, int noticeIcon, boolean isOpen, View.OnClickListener listener) {
			parentLayout.setBackgroundColor(itemView.getContext().getResources().getColor(color));
			notice.setVisibility(noticeString != 0 ? View.VISIBLE : View.GONE);
			if (noticeString != 0) notice.setText(noticeString);
			notice.setCompoundDrawablesWithIntrinsicBounds(noticeIcon, 0, 0, 0);
			notice.setPaintFlags(isOpen ? 0 : Paint.STRIKE_THRU_TEXT_FLAG);
			itemView.setOnClickListener(listener);
		}

		private void setDescription(HoverAction action) { content.setText(generateDescription(action, content.getContext())); }

		private String generateDescription(HoverAction action, Context c) {
			switch (action.transaction_type) {
				case HoverAction.AIRTIME:
					return c.getString(R.string.descrip_bounty_airtime);
				case HoverAction.P2P:
					return c.getString(R.string.descrip_bounty_p2p, (action.isOnNetwork() ? c.getString(R.string.onnet_choice) : c.getString(R.string.descrip_bounty_offnet, action.to_institution_name)));
				case HoverAction.ME2ME:
					return c.getString(R.string.descrip_bounty_me2me, action.to_institution_name);
				case HoverAction.C2B:
					return c.getString(R.string.descrip_bounty_c2b);
				default: // Balance
					return c.getString(R.string.check_balance);
			}
		}
	}

	public interface SelectListener {
		void viewTransactionDetail(String uuid);
		void bountyDetail(Bounty b);
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
