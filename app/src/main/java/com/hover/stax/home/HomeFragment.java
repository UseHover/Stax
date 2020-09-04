package com.hover.stax.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.sdk.api.HoverParameters;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.permission.PermissionScreenActivity;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.TimeAgo;
import com.hover.stax.utils.UIHelper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class HomeFragment extends Fragment {

	private HomeViewModel homeViewModel;
	private RecyclerView recyclerView;
	private TextView homeBalanceDesc, homeTimeAgo;
	private List<BalanceModel> balanceModelList;
	private List<Channel> channelList;

	private int actionRunCounter = 0;
	private final int RUN_ALL_RESULT = 302;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
		View root = inflater.inflate(R.layout.fragment_home, container, false);
		final TextView textView = root.findViewById(R.id.text_balances);
		homeBalanceDesc = root.findViewById(R.id.homeBalanceDesc);
		homeTimeAgo = root.findViewById(R.id.homeTimeAgo);

		balanceModelList = new ArrayList<>();
		homeTimeAgo.setOnClickListener(view -> {
			if (PermissionUtils.hasRequiredPermissions()) runAction(true);
			else startActivity(new Intent(getActivity(), PermissionScreenActivity.class));
		});


		textView.setOnClickListener(v -> {
			startActivity(new Intent(getActivity(), PermissionScreenActivity.class));
		});


		recyclerView = root.findViewById(R.id.balances_recyclerView);

		homeViewModel.loadChannels().observe(getViewLifecycleOwner(), channels -> {
			channelList = channels;
			homeViewModel.getBalanceFunction(channelList);
		});

		homeViewModel.loadBalance().observe(getViewLifecycleOwner(), balanceModels -> {
			setEmptyView(balanceModels.size() == 0);

			recyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
			recyclerView.setHasFixedSize(true);
			recyclerView.setAdapter(new BalanceAdapter(balanceModels));
			balanceModelList = balanceModels;

			LinkedHashSet<Long> timeStamps = new LinkedHashSet<>();
			for (BalanceModel model : balanceModelList) {
				timeStamps.add(model.getTimeStamp());
			}

			long mostRecentTimestamp = 0;
			if (timeStamps.iterator().hasNext()) mostRecentTimestamp = timeStamps.iterator().next();
			homeTimeAgo.setText(mostRecentTimestamp > 0 ? TimeAgo.timeAgo(ApplicationInstance.getContext(), mostRecentTimestamp) : "Refresh");

		});

		return root;
	}

	private void runAction(boolean firstTime) {
		if (balanceModelList.size() > 0) {
			BalanceModel balanceModel = balanceModelList.get(actionRunCounter);

			HoverParameters.Builder builder = new HoverParameters.Builder(getContext());

			builder.request(balanceModel.getActionId());
			builder.setEnvironment(HoverParameters.PROD_ENV);
			builder.style(R.style.myHoverTheme);
//        builder.initialProcessingMessage(getResources().getString(R.string.transaction_coming_up));
			builder.finalMsgDisplayTime(2000);
			builder.extra("pin", balanceModel.getChannel().pin);

			if (firstTime) actionRunCounter = actionRunCounter + 1;
			Intent i = builder.buildIntent();
			startActivityForResult(i, RUN_ALL_RESULT);
		}

	}

	private void setEmptyView(boolean status) {
		homeTimeAgo.setVisibility(status ? View.GONE : View.VISIBLE);
		homeBalanceDesc.setVisibility(status ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RUN_ALL_RESULT) {
			if (actionRunCounter < balanceModelList.size()) {
				new Handler().postDelayed(() -> {
					runAction(false);
					actionRunCounter = actionRunCounter + 1;
				}, 3000);
			} else if (actionRunCounter == balanceModelList.size()) {
				//Important to set runCounter back to zero when completed.
				actionRunCounter = 0;
				homeViewModel.getBalanceFunction(channelList);
			}
		}
	}
}
