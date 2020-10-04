package com.hover.stax.security;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.google.android.material.snackbar.Snackbar;
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

		findViewById(R.id.cancel_button).setOnClickListener(view -> {
			Amplitude.getInstance().logEvent(getString(R.string.skipped_pin_entry));
			setResult(RESULT_CANCELED);
			finish();
		});

		findViewById(R.id.continue_button).setOnClickListener(continueListener);

//		if (!((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardSecure())
//			Snackbar.make(findViewById(R.id.root), R.string.insecure_warning)
//				.setAction(R.string.skip, skipListener).show();
//		else
//			UIHelper.flashMessage(this, "Device is secure");
	}

	private View.OnClickListener continueListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			Amplitude.getInstance().logEvent(getString(R.string.completed_pin_entry));
			pinViewModel.savePins(PinsActivity.this);
			setResult(RESULT_OK);
			finish();
       }
   };

	public void onUpdate(int id, String pin) {
		pinViewModel.setPin(id, pin);
	}
}
