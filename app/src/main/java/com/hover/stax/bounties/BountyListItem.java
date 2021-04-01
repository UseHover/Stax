package com.hover.stax.bounties;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hover.stax.R;

class BountyListItem extends LinearLayout {
	private static final String TAG = "BountyListItem";

	private Bounty bounty;
	private SelectListener selectListener;

	TextView notice;

	BountyListItem(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		View v = inflate(context, R.layout.home_list_item, this);
		notice = v.findViewById(R.id.li_callout);
	}

	public void setBounty(Bounty b, SelectListener listener) {
		bounty = b;
		setContent();
		chooseState();
		selectListener = listener;
	}

	private void setContent() {
		((TextView) findViewById(R.id.li_description)).setText(bounty.generateDescription(getContext()));
		((TextView) findViewById(R.id.li_amount)).setText(getContext().getString(R.string.bounty_amount_with_currency, bounty.action.bounty_amount));
	}

	private void chooseState() {
		if (!bounty.action.bounty_is_open && bounty.transactionCount() > 0) { // Bounty is closed and done by current user
			setState(R.color.muted_green, R.string.done, R.drawable.ic_check, false,null);
		} else if (!bounty.action.bounty_is_open) { // This bounty is closed and done by another user
			setState(R.color.lighter_grey, 0, 0, false,null);
		} else if (bounty.transactionCount() > 0) { // Bounty is open and with a transaction by current user
			setState(R.color.pending_brown, R.string.bounty_pending_short_desc, R.drawable.ic_warning, true,
				(view) -> selectListener.viewTransactionDetail(bounty.transactions.get(0).uuid));
		} else
			setState(R.color.cardViewColor, 0, 0, true, (view) -> selectListener.viewBountyDetail(bounty));
	}

	private void setState(int color, int noticeString, int noticeIcon, boolean isOpen, View.OnClickListener listener) {
		setBackgroundColor(getContext().getResources().getColor(color));
		if (noticeString != 0) notice.setText(noticeString);
		notice.setCompoundDrawablesWithIntrinsicBounds(noticeIcon, 0, 0, 0);
		notice.setPaintFlags(isOpen ? 0 : Paint.STRIKE_THRU_TEXT_FLAG);
		notice.setVisibility(noticeString != 0 ? View.VISIBLE : View.GONE);
		setOnClickListener(listener);
	}

	public interface SelectListener {
		void viewTransactionDetail(String uuid);
		void viewBountyDetail(Bounty b);
	}
}
