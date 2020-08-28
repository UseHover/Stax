package com.hover.stax.security;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.pins.PinsViewModel;
import com.hover.stax.utils.UIHelper;

import java.util.ArrayList;

public class SecurityFragment extends Fragment  {
	private PinsViewModel securityViewModel;
@Nullable
@Override
public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
	securityViewModel = new ViewModelProvider(this).get(PinsViewModel.class);
	View root = inflater.inflate(R.layout.fragment_security, container, false);

	root.findViewById(R.id.removePinsButtonId).setOnClickListener(view->{
		startActivity(new Intent(getActivity(), ChannelsActivity.class));
	});

	AppCompatSpinner spinner = root.findViewById(R.id.defaultAccountSpinner);

	securityViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
		ArrayList<String> channelNames = new ArrayList<>();
		for(Channel model : channels) {
			channelNames.add(model.name);
		}
		UIHelper.loadSpinnerItems(channelNames, spinner, getContext());
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				//0 is always be the default account, so need to set for 0.
				if(position !=0) {
					Channel newDefaultChannel = channels.get(position);
					newDefaultChannel.defaultAccount = true;
					securityViewModel.setDefaultAccount(newDefaultChannel);
				}

				AppCompatTextView textView = (AppCompatTextView) parent.getChildAt(0);
				if(textView !=null){
					textView.setTextColor(getResources().getColor(R.color.white));
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});


	});
	return  root;

}

}
