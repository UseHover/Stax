package com.hover.stax.bounties;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.views.StaxCardView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class BountyArrayAdapter extends ArrayAdapter<Bounty> {
	private static final String TAG = "BountyArrayAdapter";

	private List<Bounty> bountyList;
	private final SelectListener selectListener;

	BountyArrayAdapter(@NonNull Context context, List<Bounty> bounties, SelectListener listener) {
		super(context, R.layout.home_list_item, bounties);
		bountyList = bounties;
		selectListener = listener;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View v, @NonNull ViewGroup parent) {
		if (v == null)
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_list_item, parent, false);

		Bounty b = bountyList.get(position);
		setContent(v, b);
		chooseState(v, b);
		return v;
	}

	private void setContent(View v, Bounty b) {
		((TextView) v.findViewById(R.id.li_description)).setText(b.generateDescription(v.getContext()));
		((TextView) v.findViewById(R.id.li_amount)).setText(v.getContext().getString(R.string.bounty_amount_with_currency, b.action.bounty_amount));
	}

	private void chooseState(View v, Bounty bounty) {
		if (!bounty.action.bounty_is_open && bounty.transactionCount() > 0) { // Bounty is closed and done by current user
			setState(v, R.color.muted_green, R.string.done, R.drawable.ic_check, false,null);
		} else if (!bounty.action.bounty_is_open) { // This bounty is closed and done by another user
			setState(v, R.color.lighter_grey, 0, 0, false,null);
		} else if (bounty.transactionCount() > 0) { // Bounty is open and with a transaction by current user
			setState(v, R.color.pending_brown, R.string.bounty_pending_short_desc, R.drawable.ic_warning, true,
				(view) -> selectListener.viewTransactionDetail(bounty.transactions.get(0).uuid));
		} else
			setState(v, R.color.cardViewColor, 0, 0, true, (view) -> selectListener.bountyDetail(bounty));
	}

	private void setState(View v, int color, int noticeString, int noticeIcon, boolean isOpen, View.OnClickListener listener) {
		v.setBackgroundColor(v.getContext().getResources().getColor(color));
		TextView notice = v.findViewById(R.id.li_callout);
		if (noticeString != 0) notice.setText(noticeString);
		notice.setCompoundDrawablesWithIntrinsicBounds(noticeIcon, 0, 0, 0);
		notice.setPaintFlags(isOpen ? 0 : Paint.STRIKE_THRU_TEXT_FLAG);
		notice.setVisibility(noticeString != 0 ? View.VISIBLE : View.GONE);
		v.setOnClickListener(listener);
	}

	public interface SelectListener {
		void viewTransactionDetail(String uuid);
		void bountyDetail(Bounty b);
	}

	@Override
	public long getItemId(int position) { return bountyList.get(position).action.id; }

	public int getCount() { return bountyList.size(); }

	public Bounty getItem(int position) { return bountyList.get(position); }

}
