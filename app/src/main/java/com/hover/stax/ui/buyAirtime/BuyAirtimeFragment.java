package com.hover.stax.ui.buyAirtime;

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

public class BuyAirtimeFragment extends Fragment {

private BuyAirtimeViewModel buyAirtimeViewModel;

public View onCreateView(@NonNull LayoutInflater inflater,
						 ViewGroup container, Bundle savedInstanceState) {
	buyAirtimeViewModel = new ViewModelProvider(this).get(BuyAirtimeViewModel.class);
	View root = inflater.inflate(R.layout.fragment_buyairtime, container, false);
	final TextView textView = root.findViewById(R.id.text_dashboard);
	buyAirtimeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
		@Override
		public void onChanged(@Nullable String s) {
			textView.setText(s);
		}
	});
	return root;
}
}
