package com.hover.stax.bounty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.utils.UIHelper;

public class BountyListFragment extends Fragment implements NavigationInterface, BountyListAdapter.SelectListener {
	private static final String TAG = "BountyListFragment";
	private BountyViewModel bountyViewModel;
	private View view;
	private RecyclerView sectionedBountyRecyclerView;
	private BountyRunInterface bountyRunInterface;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_bounty_main_list_layout, container, false);
		bountyViewModel = new ViewModelProvider(this).get(BountyViewModel.class);
		bountyRunInterface = (BountyRunInterface) getActivity();
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initRecyclerView();
		startObservers();
	}

	private void initRecyclerView() {
		sectionedBountyRecyclerView = view.findViewById(R.id.bountyList_main);
		sectionedBountyRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
	}

	private void startObservers() {
		bountyViewModel.getBountyActionsLiveData().observe(getViewLifecycleOwner(), sections -> {
			if (sections != null && sections.size() > 0) {
				SectionedBountyListAdapter sectionedBountyListAdapter = new SectionedBountyListAdapter(sections, this, getContext());
				sectionedBountyRecyclerView.setAdapter(sectionedBountyListAdapter);
			}
		});
	}

	@Override
	public void viewTransactionDetail(String uuid) {
	navigateToTransactionDetailsFragment(uuid, this);
	}

	@Override
	public void runAction(Action a) {
		bountyRunInterface.runAction(a);
	}
}
