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
import com.hover.stax.utils.Utils;

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
		if (ba.a.bounty_is_open == 0) { //Bounty is closed and done
			holder.parentLayout.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.muted_green));
			holder.pendingNotice.setVisibility(View.VISIBLE);
			holder.pendingNotice.setText(context.getString(R.string.done));
			holder.pendingNotice.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
			holder.itemView.setOnClickListener(null);
		} else if (ba.lastTransactionUUID != null) { //Bounty is open and with a transaction
			holder.parentLayout.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.pending_brown));
			holder.pendingNotice.setVisibility(View.VISIBLE);
			holder.pendingNotice.setText(context.getString(R.string.bounty_pending_short_desc));
			holder.pendingNotice.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_warning, 0);
			holder.pendingNotice.setOnClickListener(view -> {
				selectListener.viewTransactionDetail(ba.lastTransactionUUID);
			});
			holder.content.setOnClickListener(v -> selectListener.runAction(ba.a));
		} else {
			holder.parentLayout.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
			holder.pendingNotice.setVisibility(View.GONE);
			holder.itemView.setOnClickListener(view -> {
				selectListener.runAction(ba.a);
			});
		}
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
