package com.hover.stax.institutions;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.hover.stax.R;
import com.hover.stax.institutions.InstitutionViewModel;

public class InstitutionChoiceFragment extends Fragment {

	private InstitutionViewModel viewModel;

	public static InstitutionChoiceFragment newInstance() {
		return new InstitutionChoiceFragment();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.institution_choice_fragment, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(InstitutionViewModel.class);
//		itemSelector.setOnClickListener(item -> {
//			viewModel.select(item);
//		});
	}
}