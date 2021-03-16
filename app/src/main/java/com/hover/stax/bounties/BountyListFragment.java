package com.hover.stax.bounties;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.StaxDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BountyListFragment extends Fragment implements NavigationInterface, BountyListItem.SelectListener {
	private static final String TAG = "BountyListFragment";
	private BountyViewModel bountyViewModel;
	private View view;
	private RecyclerView channelRecyclerView;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_bounty_list, container, false);
		bountyViewModel = new ViewModelProvider(this).get(BountyViewModel.class);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initRecyclerView();
		startObservers();
	}

	private void initRecyclerView() {
		channelRecyclerView = view.findViewById(R.id.bountiesRecyclerView);
		channelRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
	}

	private void startObservers() {
		bountyViewModel.getActions().observe(getViewLifecycleOwner(), actions -> Log.v(TAG, "actions update: " + actions.size()));
		bountyViewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> Log.v(TAG, "transactions update: " + transactions.size()));
		bountyViewModel.getBounties().observe(getViewLifecycleOwner(), bounties -> {
			if (bounties != null && bounties.size() > 0 && bountyViewModel.getChannels().getValue() != null && bountyViewModel.getChannels().getValue().size() > 0)
				createList(bountyViewModel.getChannels().getValue(), bountyViewModel.getBounties().getValue());
		});

		bountyViewModel.getChannels().observe(getViewLifecycleOwner(), channels -> {
			if (channels != null && channels.size() > 0 && bountyViewModel.getBounties().getValue() != null && bountyViewModel.getBounties().getValue().size() > 0)
				createList(channels, bountyViewModel.getBounties().getValue());
		});
	}

	private void createList(List<Channel> channels, List<Bounty> bounties) {
		BountyChannelsAdapter adapter = new BountyChannelsAdapter(SectionedBounty.get(channels, bounties), this);
		channelRecyclerView.setAdapter(adapter);
	}

	@Override
	public void viewTransactionDetail(String uuid) {
		navigateToTransactionDetailsFragment(uuid, this);
	}

	@Override
	public void bountyDetail(Bounty b) {
		Log.e(TAG, "showing dialog " + b.action);
		new StaxDialog(requireActivity())
			.setDialogTitle(getString(R.string.bounty_claim_title, b.action.root_code, b.action.getHumanFriendlyType(getContext(), b.action.transaction_type), b.action.bounty_amount))
			.setDialogMessage(getString(R.string.bounty_claim_explained, b.action.bounty_amount, b.getInstructions(getContext())))
			.setPosButton(R.string.start_USSD_Flow, v -> {
				if (getActivity() != null)
					((BountyActivity) getActivity()).makeCall(b.action);
			})
			.showIt();
	}
}
