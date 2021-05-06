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

import com.amplitude.api.Amplitude;
import com.hover.sdk.api.Hover;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.countries.CountryAdapter;
import com.hover.stax.countries.CountryDropdown;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.StaxDialog;

import java.util.List;

public class BountyListFragment extends Fragment implements NavigationInterface, BountyListItem.SelectListener, CountryAdapter.SelectListener {
	private static final String TAG = "BountyListFragment";

	private BountyViewModel bountyViewModel;
	private RecyclerView channelRecyclerView;
	private CountryDropdown countryDropdown;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_bounty_list)));
		bountyViewModel = new ViewModelProvider(this).get(BountyViewModel.class);
		return inflater.inflate(R.layout.fragment_bounty_list, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initCountryDropdown(view);
		initRecyclerView(view);
		startObservers();
	}

	public void initCountryDropdown(View view) {
		countryDropdown = view.findViewById(R.id.bounty_country_dropdown);
		countryDropdown.setListener(this);
	}

	private void initRecyclerView(View view) {
		channelRecyclerView = view.findViewById(R.id.bountiesRecyclerView);
		channelRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
	}

	private void startObservers() {
		bountyViewModel.getActions().observe(getViewLifecycleOwner(), actions -> Log.v(TAG, "actions update: " + actions.size()));
		bountyViewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> Log.v(TAG, "transactions update: " + transactions.size()));
		bountyViewModel.getSims().observe(getViewLifecycleOwner(), sims -> Log.v(TAG, "sim update: " + sims.size()));
		bountyViewModel.getBounties().observe(getViewLifecycleOwner(), bounties -> updateChannelList(bountyViewModel.getChannels().getValue(), bounties));

		bountyViewModel.getChannels().observe(getViewLifecycleOwner(), channels -> {
			countryDropdown.updateChoices(channels);
			updateChannelList(channels, bountyViewModel.getBounties().getValue());
		});


	}

	private void updateChannelList(List<Channel> channels, List<Bounty> bounties) {
		if (bounties != null && bounties.size() > 0 && channels != null && channels.size() > 0) {
			BountyChannelsAdapter adapter = new BountyChannelsAdapter(channels, bounties, this);
			channelRecyclerView.setAdapter(adapter);
		}
	}

	@Override
	public void viewTransactionDetail(String uuid) {
		navigateToTransactionDetailsFragment(uuid, this);
	}

	@Override
	public void viewBountyDetail(Bounty b) {
		if (bountyViewModel.isSimPresent(b)) showBountyDescDialog(b);
		else showSimErrorDialog(b);
	}

	void showSimErrorDialog(Bounty b) {
		Log.e(TAG, "showing sim error dialog "+ b.action.root_code);
		new StaxDialog(requireActivity())
				.setDialogTitle(getString(R.string.bounty_sim_err_header, b.action.network_name))
				.setDialogMessage(getString(R.string.bounty_sim_err_desc, b.action.network_name))
				.setNegButton(R.string.btn_cancel, null)
				.setPosButton(R.string.retry, v -> retrySimMatch(b))
				.showIt();
	}

	void showBountyDescDialog(Bounty b) {
		Log.e(TAG, "showing dialog " + b.action);
		new StaxDialog(requireActivity())
			.setDialogTitle(getString(R.string.bounty_claim_title, b.action.root_code, b.action.getHumanFriendlyType(getContext(), b.action.transaction_type), b.action.bounty_amount))
			.setDialogMessage(getString(R.string.bounty_claim_explained, b.action.bounty_amount, b.getInstructions(getContext())))
			.setPosButton(R.string.start_USSD_Flow, v -> {
					((BountyActivity) requireActivity()).makeCall(b.action);
			})
			.showIt();
	}

	void retrySimMatch(Bounty b) {
		bountyViewModel.getSims().removeObservers(getViewLifecycleOwner());
		bountyViewModel.getSims().observe(getViewLifecycleOwner(), sims -> viewBountyDetail(b));
		Hover.updateSimInfo(getContext());
	}

	@Override
	public void countrySelect(String countryCode) {
		bountyViewModel.filterChannels(countryCode).observe(getViewLifecycleOwner(), channels ->
			updateChannelList(channels, bountyViewModel.getBounties().getValue()));
	}
}
