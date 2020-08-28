package com.hover.stax.pins;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.MainActivity;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.utils.UIHelper;

public class PinsActivity extends AppCompatActivity {

	private PinsViewModel channelViewModel;
	private PinEntryAdapter pinEntryAdapter;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.services_pin_layout);
		channelViewModel = new ViewModelProvider(this).get(PinsViewModel.class);


		RecyclerView pinRecyclerView = findViewById(R.id.pin_recyclerView);
		channelViewModel.getSelectedChannels().observe(this, channels -> {

			for(Channel c: channels) {
				Log.d("SWEET", c.pin == null ? "null" : c.pin);
			}
			pinRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(this));
			pinRecyclerView.setHasFixedSize(true);
			pinEntryAdapter = new PinEntryAdapter(channels);

			pinRecyclerView.setAdapter(pinEntryAdapter);
		});

		findViewById(R.id.choose_serves_cancel).setOnClickListener(view -> finish());

		findViewById(R.id.continuePinButton).setOnClickListener(view -> {
			channelViewModel.savePins(pinEntryAdapter.retrieveChannels(), this);
			MainActivity.GO_TO_SPLASH_SCREEN = false;
			startActivity(new Intent(this, MainActivity.class));
		});


	}
}
