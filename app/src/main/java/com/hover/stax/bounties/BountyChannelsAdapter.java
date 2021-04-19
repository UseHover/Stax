package com.hover.stax.bounties;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.channels.Channel;
import com.hover.stax.databinding.BountyCardChannelBinding;

import java.util.ArrayList;
import java.util.List;

class BountyChannelsAdapter extends RecyclerView.Adapter<BountyChannelsAdapter.CardedBountyListViewHolder> {

	private final List<Channel> channelList;
	private final List<Bounty> allBountiesList;
	private final BountyListItem.SelectListener selectListener;

	public BountyChannelsAdapter(List<Channel> channels, List<Bounty> bounties, BountyListItem.SelectListener listener) {
		channelList = channels;
		allBountiesList = bounties;
		selectListener = listener;
	}

	@NonNull
	@Override
	public CardedBountyListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		BountyCardChannelBinding binding = BountyCardChannelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		return new CardedBountyListViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull CardedBountyListViewHolder holder, int position) {
		Channel c = channelList.get(position);
		holder.binding.bountyChannelCard.setTitle(c.getUssdName());
		List<Bounty> channelBounties = filterBounties(c.id);

		for (Bounty b: channelBounties) {
			BountyListItem bountyLi = new BountyListItem(holder.binding.bountyChannelCard.getContext(), null);
			bountyLi.setBounty(b, selectListener);
			holder.binding.bountyList.addView(bountyLi);
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
		public BountyCardChannelBinding binding;

		public CardedBountyListViewHolder(BountyCardChannelBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
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