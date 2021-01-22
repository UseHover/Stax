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

public class HomeFragment extends Fragment {
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_home)));
		return inflater.inflate(R.layout.fragment_main, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		HomeNavigationListener homeNavigationListener = ((MainActivity) getActivity());
		if(homeNavigationListener !=null) {
			view.findViewById(R.id.airtime).setOnClickListener(v-> homeNavigationListener.goToBuyAirtimeScreen(R.id.airtime));
			view.findViewById(R.id.transfer).setOnClickListener(v->homeNavigationListener.goToSendMoneyScreen(R.id.transfer));
			view.findViewById(R.id.request).setOnClickListener(v->homeNavigationListener.goToRequestMoneyScreen(R.id.request));
		}
	}
}
