package com.hover.stax.settings;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.amplitude.api.Amplitude;
import com.google.android.material.textfield.TextInputEditText;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.StaxDialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class PinUpdateFragment extends Fragment implements Target {
	private static String TAG = "PinUpdateFragment";

	private View view;
	private TextInputEditText input;
	PinsViewModel pinViewModel;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_change_pin)));
		view = inflater.inflate(R.layout.fragment_pin_update, container, false);

		pinViewModel = new ViewModelProvider(this).get(PinsViewModel.class);
		pinViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> Log.e(TAG, "Observer ensures events fire."));
		pinViewModel.loadChannel(getArguments().getInt("channel_id", 0));
		pinViewModel.getChannel().observe(getViewLifecycleOwner(), this::initView);

		input = view.findViewById(R.id.pin_input);
		view.findViewById(R.id.editBtn).setOnClickListener(v -> showChoiceCard(false));
		view.findViewById(R.id.cancelBtn).setOnClickListener(v -> showChoiceCard(true));

		return inflater.inflate(R.layout.fragment_pin_update, container, false);
	}

	private void initView(Channel c) {
		if (c == null) { return; }
		((TextView) view.findViewById(R.id.choice_card).findViewById(R.id.title)).setText(c.name);
		((TextView) view.findViewById(R.id.edit_card).findViewById(R.id.title)).setText(c.name);
		Picasso.get().load(c.logoUrl).into(PinUpdateFragment.this);
		if (c.pin != null && !c.pin.isEmpty())
			input.setText(KeyStoreExecutor.decrypt(c.pin, getContext()));
		setupSavePin(c);
		setUpRemoveAccount(c);
	}

	private void setupSavePin(Channel channel) {
		view.findViewById(R.id.saveBtn).setOnClickListener(v -> {
			if (input.getText() != null) {
				channel.pin = input.getText().toString();
				pinViewModel.savePin(channel, getContext());
			}
			UIHelper.flashMessage(view.getContext(), getResources().getString(R.string.toast_confirm_pinupdate));
			showChoiceCard(true);
		});
	}

	private void setUpRemoveAccount(Channel channel) {
		view.findViewById(R.id.removeAcct).setOnClickListener(v -> {
			new StaxDialog(getContext(), this)
				.setDialogTitle(getContext().getString(R.string.removepin_dialoghead, channel.name))
				.setDialogMessage(R.string.removepins_dialogmes)
				.setPosButton(R.string.btn_removeaccount, btn -> removeAccount(channel))
				.setNegButton(R.string.btn_cancel, null)
				.isDestructive()
				.showIt();
		});
	}

	private void removeAccount(Channel channel) {
		pinViewModel.removeAccount(channel);
		NavHostFragment.findNavController(PinUpdateFragment.this).popBackStack();
		UIHelper.flashMessage(getContext(), getResources().getString(R.string.toast_confirm_acctremoved));
	}

	private void showChoiceCard(boolean show) {
		view.findViewById(R.id.choice_card).setVisibility(show ? View.VISIBLE : View.GONE);
		view.findViewById(R.id.edit_card).setVisibility(show ? View.GONE : View.VISIBLE);
		if (!show) input.requestFocus();
	}

	@Override
	public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
		Bitmap b = Bitmap.createScaledBitmap(bitmap, UIHelper.dpToPx(34), UIHelper.dpToPx(34), true);
		RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(view.getContext().getResources(), b);
		d.setCircular(true);
		input.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
	}

	@Override public void onBitmapFailed(Exception e, Drawable errorDrawable) {	}
	@Override public void onPrepareLoad(Drawable placeHolderDrawable) {	}
}
