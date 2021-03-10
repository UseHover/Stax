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
import com.hover.stax.utils.UIHelper;

public class BountyListFragment extends Fragment implements BountyListAdapter.SelectListener {
	private static final String TAG = "BountyListFragment";
	private BountyViewModel bountyViewModel;
	private View view;
	private RecyclerView bountyRecyclerView;
	private BountyRunInterface bountyRunInterface;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_bounty_list_layout, container, false);
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
		bountyRecyclerView = view.findViewById(R.id.bountyList_recyclerView_id);
		bountyRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
	}

	private void startObservers() {
		bountyViewModel.getFilteredBountyActionsLiveData().observe(getViewLifecycleOwner(), actions -> {
			if (actions != null && actions.size() > 0) {
				BountyListAdapter bountyListAdapter = new BountyListAdapter(actions, this, getContext());
				bountyRecyclerView.setAdapter(bountyListAdapter);
			}
		});
	}

	@Override
	public void viewTransactionDetail(String uuid) {

	}

	@Override
	public void runAction(Action a) {
		bountyRunInterface.runAction(a);
	}
}
