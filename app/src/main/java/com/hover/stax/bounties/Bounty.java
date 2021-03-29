package com.hover.stax.bounties;

import android.content.Context;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.transactions.StaxTransaction;

import java.util.List;

public class Bounty {
	HoverAction action;
	List<StaxTransaction> transactions;

	public Bounty(HoverAction a, List<StaxTransaction> ts) {
		action = a;
		transactions = ts;
	}

	public HoverAction getAction() {
		return action;
	}

	public int transactionCount() { return transactions.size(); }

	String generateDescription(Context c) {
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

	public String getInstructions(Context c) {
		switch (action.transaction_type) {
			case HoverAction.AIRTIME:
				return c.getString(R.string.bounty_airtime_explain);
			case HoverAction.P2P:
				return c.getString(R.string.bounty_p2p_explain, (action.isOnNetwork() ? c.getString(R.string.onnet_choice) : c.getString(R.string.descrip_bounty_offnet, action.to_institution_name)));
			case HoverAction.ME2ME:
				return c.getString(R.string.bounty_me2me_explain, action.to_institution_name);
			case HoverAction.C2B:
				return c.getString(R.string.bounty_c2b_explain);
			default: // Balance
				return c.getString(R.string.bounty_balance_explain);
		}
	}
}
