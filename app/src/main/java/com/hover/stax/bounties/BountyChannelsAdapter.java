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

	private List<SectionedBounty> sectionedBounties;
	private BountyListItem.SelectListener selectListener;

	public BountyChannelsAdapter(List<SectionedBounty> sectionedBounties, BountyListItem.SelectListener listener) {
		this.sectionedBounties = sectionedBounties;
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
		SectionedBounty sectionedBounty = sectionedBounties.get(position);
		holder.staxCardView.setTitle(sectionedBounty.header);
		for (Bounty b: sectionedBounty.channelBounties) {
			BountyListItem bountyLi = new BountyListItem(holder.staxCardView.getContext(), null);
			bountyLi.setBounty(b, selectListener);
			holder.bountyListView.addView(bountyLi);
		}
	}

	@Override
	public int getItemCount() {
		return sectionedBounties == null ? 0 : sectionedBounties.size();
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