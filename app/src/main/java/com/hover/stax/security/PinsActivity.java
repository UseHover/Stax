package com.hover.stax.security;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.hover.stax.home.MainActivity;
import com.hover.stax.R;
import com.hover.stax.utils.UIHelper;

public class PinsActivity extends AppCompatActivity implements PinEntryAdapter.UpdateListener {

	private PinsViewModel pinViewModel;
	private PinEntryAdapter pinEntryAdapter;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pins_entry_layout);
		pinViewModel = new ViewModelProvider(this).get(PinsViewModel.class);

		RecyclerView pinRecyclerView = findViewById(R.id.pin_recyclerView);
		pinViewModel.getSelectedChannels().observe(this, channels -> {
			pinRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(this));
			pinRecyclerView.setHasFixedSize(true);
			pinEntryAdapter = new PinEntryAdapter(channels, this);
			pinRecyclerView.setAdapter(pinEntryAdapter);
		});

		findViewById(R.id.choose_serves_cancel).setOnClickListener(view -> {
			Amplitude.getInstance().logEvent(getString(R.string.skipped_pin_entry));
			finish();
		});

		findViewById(R.id.continuePinButton).setOnClickListener(view -> {
			Amplitude.getInstance().logEvent(getString(R.string.completed_pin_entry));
			pinViewModel.savePins(this);
			Intent i = new Intent(this, MainActivity.class);
			i.setAction(MainActivity.CHECK_ALL_BALANCES);
			startActivity(i);
		});
	}

	public void onUpdate(int id, String pin) {
		pinViewModel.setPin(id, pin);
	}
}
