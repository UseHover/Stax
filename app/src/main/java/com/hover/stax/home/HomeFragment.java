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
import com.hover.stax.databinding.FragmentMainBinding;
import com.hover.stax.utils.Constants;

public class HomeFragment extends Fragment {

    private FragmentMainBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_home)));

        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.airtime.setOnClickListener(v -> navigateTo(Constants.NAV_AIRTIME));
        binding.transfer.setOnClickListener(v -> navigateTo(Constants.NAV_TRANSFER));
        binding.request.setOnClickListener(v -> navigateTo(Constants.NAV_REQUEST));
    }

    @Override
    public void onStart() {
        super.onStart();

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(null);
    }

    private void navigateTo(int destination) {
        MainActivity act = ((MainActivity) getActivity());
        if (act != null) act.checkPermissionsAndNavigate(destination);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }
}
