package com.hover.stax.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.permission.PermissionScreenActivity;
import com.hover.stax.utils.UIHelper;

import java.util.List;

public class HomeFragment extends Fragment {

private HomeViewModel homeViewModel;
private RecyclerView recyclerView;
private TextView homeBalanceDesc, homeTimeAgo;


public View onCreateView(@NonNull LayoutInflater inflater,
						 ViewGroup container, Bundle savedInstanceState) {
	homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
	View root = inflater.inflate(R.layout.fragment_home, container, false);
	final TextView textView = root.findViewById(R.id.text_balances);
	homeBalanceDesc = root.findViewById(R.id.homeBalanceDesc);
	homeTimeAgo = root.findViewById(R.id.homeTimeAgo);


	textView.setOnClickListener(v -> {
		startActivity(new Intent(getActivity(), PermissionScreenActivity.class));
	});


	recyclerView = root.findViewById(R.id.balances_recyclerView);

	homeViewModel.loadChannels().observe(getViewLifecycleOwner(), channels ->{
		homeViewModel.getBalanceFunction(channels);
	});

	homeViewModel.loadBalance().observe(getViewLifecycleOwner(), balanceModels -> {
		Log.d("sweet", "number of balances are: "+balanceModels.size());
		setEmptyView(balanceModels.size() == 0);

		recyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
		recyclerView.setHasFixedSize(true);
		recyclerView.setAdapter(new BalanceAdapter(balanceModels));
	});

	return root;
}

private void setEmptyView(boolean status) {
	homeTimeAgo.setVisibility(status ? View.GONE : View.VISIBLE);
	homeBalanceDesc.setVisibility(status ? View.VISIBLE: View.GONE);
}
}
