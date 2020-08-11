package com.hover.stax.ui.moveMoney;

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
import androidx.lifecycle.ViewModelProviders;

import com.hover.stax.R;

public class MoveMoneyFragment extends Fragment {

private MoveMoneyViewModel moveMoneyViewModel;

public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	moveMoneyViewModel = new ViewModelProvider(this).get(MoveMoneyViewModel.class);
	View root = inflater.inflate(R.layout.fragment_movemoney, container, false);
	final TextView textView = root.findViewById(R.id.text_notifications);
	moveMoneyViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
		@Override
		public void onChanged(@Nullable String s) {
			textView.setText(s);
		}
	});
	return root;
}
}
