package com.hover.stax.security;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hover.sdk.actions.ActionContract;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.utils.UIHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class PinUpdateFragment extends Fragment implements Target {
	private TextInputLayout label;
	private TextInputEditText input;
	private View view;
	private TextView titleText, cancelUpdatePin;
	private Button savePinButton;
	private AppCompatButton removeAccountButton;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (getArguments() == null) {
			if (getActivity() != null && getContext() != null) {
				UIHelper.flashMessage(getContext(), getResources().getString(R.string.account_not_found));
				getActivity().onBackPressed();
			}
		}
		int channel_id = getArguments().getInt(ActionContract.COLUMN_CHANNEL_ID, 0);
		PinsViewModel pinViewModel = new ViewModelProvider(this).get(PinsViewModel.class);
		pinViewModel.loadChannel(channel_id);

		view = inflater.inflate(R.layout.pin_update_layout, container, false);
		init(view);
		loadPin(pinViewModel);
		setupCancel();

		return view;
	}

	private void init(View view) {
		label = view.findViewById(R.id.pinEntry);
		input = view.findViewById(R.id.pin_input);
		titleText = view.findViewById(R.id.title);
		cancelUpdatePin = view.findViewById(R.id.cancelUpdatePin);
		savePinButton = view.findViewById(R.id.save_pin_button_id);
		removeAccountButton = view.findViewById(R.id.removeAccountButtonId);
	}

	private void loadPin(PinsViewModel pinsViewModel) {
		pinsViewModel.getChannel().observe(getViewLifecycleOwner(), channel -> {
			if (channel != null) {
				titleText.setText(channel.name);
				Picasso.get().load(channel.logoUrl).into(PinUpdateFragment.this);
				label.setHint(channel.name);
				if (channel.pin != null && !channel.pin.isEmpty()) {
					input.setText(KeyStoreExecutor.decrypt(channel.pin, getContext()));
				}
				setupSavePin(pinsViewModel, channel);
				setUpRemoveAccount(pinsViewModel, channel);
			}

		});
	}

	private void setupCancel() {
		cancelUpdatePin.setOnClickListener(v -> {
			if (getActivity() != null) getActivity().onBackPressed();
		});
	}

	private void setupSavePin(PinsViewModel pinsViewModel, Channel channel) {
		savePinButton.setOnClickListener(v -> {
			if (input.getText() != null) {
				channel.pin = input.getText().toString();
				pinsViewModel.savePin(channel, getContext());
			}
			if (getActivity() != null && getContext() != null) {
				UIHelper.flashMessage(getContext(), getResources().getString(R.string.pin_updated));
				getActivity().onBackPressed();
			}
		});
	}

	void setUpRemoveAccount(PinsViewModel pinsViewModel, Channel channel) {
		removeAccountButton.setOnClickListener(v -> {
			pinsViewModel.removeAccount(channel);
			if (getActivity() != null && getContext() != null) {
				UIHelper.flashMessage(getContext(), getResources().getString(R.string.account_removed));
				getActivity().onBackPressed();
			}
		});
	}

	@Override
	public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
		Bitmap b = Bitmap.createScaledBitmap(bitmap, UIHelper.dpToPx(34), UIHelper.dpToPx(34), true);
		RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(view.getContext().getResources(), b);
		d.setCircular(true);
		input.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
	}

	@Override
	public void onBitmapFailed(Exception e, Drawable errorDrawable) {

	}

	@Override
	public void onPrepareLoad(Drawable placeHolderDrawable) {

	}
}
