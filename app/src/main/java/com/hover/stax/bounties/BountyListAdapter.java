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

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.utils.Constants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class BountyListAdapter extends RecyclerView.Adapter<BountyListAdapter.BountyListViewHolder> {
	private static final String TAG = "BountyListAdapter";

	private LinkedHashMap<Action, List<StaxTransaction>> bountyMap;
	private final SelectListener selectListener;

	BountyListAdapter(Map<Action, List<StaxTransaction>> bounties, SelectListener listener) {
		bountyMap = new LinkedHashMap<>(bounties);
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
		Action a = (Action) bountyMap.keySet().toArray()[position];
		chooseState(holder, a, (List<StaxTransaction>) bountyMap.entrySet().toArray()[position]);
		holder.setDescription(a);
		holder.amount.setText(holder.itemView.getContext().getString(R.string.bounty_amount_with_currency, a.bounty_amount));
	}

	private void chooseState(BountyListViewHolder holder, Action action, List<StaxTransaction> transactions) {
		if (action.bounty_is_open == 0 && transactions.size() > 0) { // Bounty is closed and done by current user
			holder.setState(R.color.muted_green, R.string.done, R.drawable.ic_check, false,null);
		} else if (action.bounty_is_open == 0) { // This bounty is closed and done by another user
			holder.setState(R.color.lighter_grey, 0, 0, false,null);
		} else if (transactions.size() > 0) { // Bounty is open and with a transaction by current user
			holder.setState(R.color.pending_brown, R.string.bounty_pending_short_desc, R.drawable.ic_warning, true,
				(view) -> selectListener.viewTransactionDetail(transactions.get(0).uuid));
		} else
			holder.setState(R.color.colorPrimary, 0, 0, true, (view) -> selectListener.runAction(action));
	}

	@Override
	public int getItemCount() {
		return bountyMap != null ? bountyMap.size() : 0;
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
			notice.setText(noticeString);
			notice.setCompoundDrawablesWithIntrinsicBounds(noticeIcon, 0, 0, 0);
			notice.setPaintFlags(isOpen ? 0 : Paint.STRIKE_THRU_TEXT_FLAG);
			itemView.setOnClickListener(listener);
		}

		private void setDescription(Action action) { content.setText(generateDescription(action, content.getContext())); }

		private String generateDescription(Action action, Context c) {
			switch (action.transaction_type) {
				case Action.AIRTIME:
					return c.getString(R.string.descrip_bounty_airtime);
				case Action.P2P:
					return c.getString(R.string.descrip_bounty_p2p, (action.isOnNetwork() ? c.getString(R.string.onnet_choice) : c.getString(R.string.descrip_bounty_offnet, action.to_institution_name)));
				case Action.ME2ME:
					return c.getString(R.string.descrip_bounty_me2me, action.to_institution_name);
				case Action.C2B:
					return c.getString(R.string.descrip_bounty_c2b);
				default: // Balance
					return c.getString(R.string.check_balance);
			}
		}
	}

	public interface SelectListener {
		void viewTransactionDetail(String uuid);
		void runAction(Action a);
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
