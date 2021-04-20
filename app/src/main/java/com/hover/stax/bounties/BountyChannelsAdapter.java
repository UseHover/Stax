package com.hover.stax.bounties;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.views.StaxCardView;

import java.util.ArrayList;
import java.util.List;

class BountyChannelsAdapter extends RecyclerView.Adapter<BountyChannelsAdapter.CardedBountyListViewHolder> {

	private List<Channel> channelList;
	private List<Bounty> allBountiesList;
	private BountyListItem.SelectListener selectListener;

	public BountyChannelsAdapter(List<Channel> channels, List<Bounty> bounties, BountyListItem.SelectListener listener) {
		channelList = channels;
		allBountiesList = bounties;
		selectListener = listener;
	}

	@NonNull
	@Override
	public CardedBountyListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bounty_card_channel, parent, false);
		return new CardedBountyListViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull CardedBountyListViewHolder holder, int position) {
		Channel c = channelList.get(position);
		holder.staxCardView.setTitle(c.getUssdName());
		List<Bounty> channelBounties = filterBounties(c.id);
		for (Bounty b: channelBounties) {
			BountyListItem bountyLi = new BountyListItem(holder.staxCardView.getContext(), null);
			bountyLi.setBounty(b, selectListener);
			holder.bountyListView.addView(bountyLi);
		}
	}

	private List<Bounty> filterBounties(int channelId) {
		List<Bounty> matches = new ArrayList<>();
		for (Bounty b: allBountiesList) {
			if (b.action.channel_id == channelId)
				matches.add(b);
		}
		return matches;
	}

	@Override
	public int getItemCount() {
		return channelList == null ? 0 : channelList.size();
	}

	static class CardedBountyListViewHolder extends RecyclerView.ViewHolder {
		private StaxCardView staxCardView;
		private LinearLayout bountyListView;

		public CardedBountyListViewHolder(@NonNull View itemView) {
			super(itemView);
			staxCardView = itemView.findViewById(R.id.bountyChannelCard);
			bountyListView = itemView.findViewById(R.id.bountyList);
		}
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