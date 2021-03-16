package com.hover.stax.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.utils.Constants;

public class HomeFragment extends Fragment {
	private View view;
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_home)));
		view = inflater.inflate(R.layout.fragment_main, container, false);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.airtime).setOnClickListener(v -> navigateTo(Constants.NAV_AIRTIME));
		view.findViewById(R.id.transfer).setOnClickListener(v -> navigateTo(Constants.NAV_TRANSFER));
		view.findViewById(R.id.request).setOnClickListener(v -> navigateTo(Constants.NAV_REQUEST));
	}

	private void navigateTo(int destination) {
		MainActivity act = ((MainActivity) getActivity());
		if (act != null) act.checkPermissionsAndNavigate(destination);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		view = null;
	}
}
