package com.hover.stax.security;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.utils.UIHelper;

public class PinEntryFragment extends Fragment implements PinEntryAdapter.UpdateListener {
	private PinsViewModel viewModel;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		viewModel = new ViewModelProvider(requireActivity()).get(PinsViewModel.class);
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_choose_channels)));
		return inflater.inflate(R.layout.fragment_pins, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		((TextView) view.findViewById(R.id.pin_explainer)).setText(Html.fromHtml(getString(R.string.pin_security_description)));

		RecyclerView pinRecyclerView = view.findViewById(R.id.pin_recyclerView);
		viewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			if (channels == null || channels.size() == 0) { return; }

			pinRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
			pinRecyclerView.setHasFixedSize(true);
			PinEntryAdapter pinEntryAdapter = new PinEntryAdapter(channels, this);
			pinRecyclerView.setAdapter(pinEntryAdapter);

			for (Channel c: channels)
				if (c.pin == null) { return; }
			if (getActivity() != null)
				((PinsActivity) getActivity()).balanceAsk();
		});

//		if (!((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardSecure())
//			Snackbar.make(findViewById(R.id.root), R.string.insecure_warning)
//				.setAction(R.string.skip, skipListener).show();
//		else
//			UIHelper.flashMessage(this, "Device is secure");

	}

	public void onUpdate(int id, String pin) {
		viewModel.setPin(id, pin);
	}
}
