package com.hover.stax.bounty;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.StaxCardView;

import java.util.List;

class SectionedBountyListAdapter extends RecyclerView.Adapter<SectionedBountyListAdapter.CardedBountyListViewHolder> {
	private Context context;
	private List<SectionedBountyAction> sectionedBountyActions;
	private BountyListAdapter.SelectListener selectListener;

	public SectionedBountyListAdapter(List<SectionedBountyAction> sectionedBountyActions, BountyListAdapter.SelectListener selectListener, Context context) {
		this.context = context;
		this.sectionedBountyActions = sectionedBountyActions;
		this.selectListener = selectListener;
	}

	@NonNull
	@Override
	public CardedBountyListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bounty_inner_list_layout, parent, false);
		return new CardedBountyListViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull CardedBountyListViewHolder holder, int position) {
		SectionedBountyAction bountyActionSection = sectionedBountyActions.get(position);
		if (bountyActionSection.bountyActionList != null && bountyActionSection.bountyActionList.size() > 0) {
			RecyclerView bountyRecyclerView = holder.innerRecyclerView;
			bountyRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(context));
			BountyListAdapter bountyListAdapter = new BountyListAdapter(bountyActionSection.bountyActionList, selectListener, context);
			bountyRecyclerView.setAdapter(bountyListAdapter);
		}
		holder.staxCardView.setHeader(bountyActionSection.header);
	}

	@Override
	public int getItemCount() {
		if(sectionedBountyActions == null) return  0;
		return sectionedBountyActions.size();
	}

	static class CardedBountyListViewHolder extends RecyclerView.ViewHolder {
		private RecyclerView innerRecyclerView;
		private StaxCardView staxCardView;
		public CardedBountyListViewHolder(@NonNull View itemView) {
			super(itemView);
			innerRecyclerView = itemView.findViewById(R.id.bountyList_recyclerView_id);
			staxCardView = itemView.findViewById(R.id.bounty_sectioned_list);
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