package com.hover.stax.bounty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.utils.UIHelper;

import java.util.List;

class BountyListAdapter extends RecyclerView.Adapter<BountyListAdapter.BountyListViewHolder> {
	private static final String TAG = "BountyListAdapter";
	private List<BountyAction> bountyActionList;
	private final SelectListener selectListener;
	private final Context context;

	BountyListAdapter(List<BountyAction> actionList, SelectListener selectListener, Context context) {
		this.bountyActionList = actionList;
		this.selectListener = selectListener;
		this.context = context;
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
		BountyAction ba = bountyActionList.get(position);
		Action a = ba.a;
		updateStatusLayoutAndSetClickListener(holder, ba);
		holder.content.setText(a.getFullDescription(context));
		holder.amount.setText(a.getBountyAmountWithCurrency(context));
	}

	private void updateStatusLayoutAndSetClickListener(BountyListViewHolder holder, BountyAction ba) {
		if (ba.a.bounty_is_open == 0 && ba.lastTransactionUUID !=null) { //Bounty is closed and done by current user
			updateBgColor(holder, R.color.muted_green);
			UIHelper.setTextWithDrawable(holder.pendingNotice, context.getString(R.string.done), R.drawable.ic_check, View.VISIBLE);
			UIHelper.strikeThroughTextView(holder.content, holder.amount);
			holder.itemView.setOnClickListener(null);
		}
		else if(ba.a.bounty_is_open == 0) { //This bounty is closed and done by another user
			updateBgColor(holder, R.color.lighter_grey);
			UIHelper.setTextWithDrawable(holder.pendingNotice, "", 0, View.GONE);
			holder.itemView.setOnClickListener(null);
			UIHelper.strikeThroughTextView(holder.content, holder.amount);
		}
		else if (ba.lastTransactionUUID != null) { //Bounty is open and with a transaction by current user
			updateBgColor(holder, R.color.pending_brown);
			UIHelper.setTextWithDrawable(holder.pendingNotice, context.getString(R.string.bounty_pending_short_desc), R.drawable.ic_warning, View.VISIBLE);
			holder.pendingNotice.setOnClickListener(view -> { selectListener.viewTransactionDetail(ba.lastTransactionUUID); });
			holder.content.setOnClickListener(v -> selectListener.runAction(ba.a));
		}
		else { //default state
			updateBgColor(holder,R.color.colorPrimary);
			UIHelper.setTextWithDrawable(holder.pendingNotice, "", 0, View.GONE);
			holder.itemView.setOnClickListener(view -> { selectListener.runAction(ba.a); });
		}
	}
	private void updateBgColor(BountyListViewHolder holder, int color) {
		if(color!=0)holder.parentLayout.setBackgroundColor(holder.itemView.getContext().getResources().getColor(color));
	}

	@Override
	public int getItemCount() {
		return bountyActionList != null ? bountyActionList.size() : 0;
	}

	static class BountyListViewHolder extends RecyclerView.ViewHolder {
		private TextView content, amount, pendingNotice;
		private LinearLayout parentLayout;

		public BountyListViewHolder(@NonNull View itemView) {
			super(itemView);
			parentLayout = itemView.findViewById(R.id.transaction_item_layout);
			content = itemView.findViewById(R.id.li_description);
			amount = itemView.findViewById(R.id.li_amount);
			pendingNotice = itemView.findViewById(R.id.li_callout);
			TextView date = itemView.findViewById(R.id.li_header);
			date.setVisibility(View.GONE);
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
