package com.hover.stax.ui.security;

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

public class SecurityFragment extends Fragment {
	private SecurityViewModel securityViewModel;
@Nullable
@Override
public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
	securityViewModel = new ViewModelProvider(this).get(SecurityViewModel.class);
	View root = inflater.inflate(R.layout.fragment_security, container, false);
	final TextView textView = root.findViewById(R.id.text_notifications);

	securityViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
		@Override
		public void onChanged(String s) {
			textView.setText(s);
		}
	});
	return  root;

}
}
