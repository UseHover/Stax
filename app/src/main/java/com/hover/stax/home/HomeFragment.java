package com.hover.stax.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hover.stax.R;

public class HomeFragment extends Fragment {

private HomeViewModel homeViewModel;

public View onCreateView(@NonNull LayoutInflater inflater,
						 ViewGroup container, Bundle savedInstanceState) {
	homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
	View root = inflater.inflate(R.layout.fragment_home, container, false);
	//final TextView textView = root.findViewById(R.id.text_home);

	return root;
}
}
