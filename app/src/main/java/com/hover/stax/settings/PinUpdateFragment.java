package com.hover.stax.settings;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.hover.stax.databinding.FragmentPinUpdateBinding;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.StaxDialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import timber.log.Timber;

public class PinUpdateFragment extends Fragment implements Target {

	private TextInputEditText input;
	PinsViewModel pinViewModel;

	private FragmentPinUpdateBinding binding;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_change_pin)));

		binding = FragmentPinUpdateBinding.inflate(inflater, container, false);

		pinViewModel = new ViewModelProvider(this).get(PinsViewModel.class);
		pinViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> Timber.e("Observer ensures events fire."));
		pinViewModel.loadChannel(getArguments().getInt("channel_id", 0));
		pinViewModel.getChannel().observe(getViewLifecycleOwner(), this::initView);

		input = binding.pinInput;
		binding.editBtn.setOnClickListener(v -> showChoiceCard(false));
		binding.cancelBtn.setOnClickListener(v -> showChoiceCard(true));

		return binding.getRoot();
	}

	private void initView(Channel c) {
		if (c == null) { return; }
		binding.choiceCard.setTitle(c.name);
		binding.editCard.setTitle(c.name);
		Picasso.get().load(c.logoUrl).into(PinUpdateFragment.this);
		if (c.pin != null && !c.pin.isEmpty())
			input.setText(KeyStoreExecutor.decrypt(c.pin, getContext()));
		setupSavePin(c);
		setUpRemoveAccount(c);
	}

	private void setupSavePin(Channel channel) {
		binding.saveBtn.setOnClickListener(v -> {
			if (input.getText() != null) {
				channel.pin = input.getText().toString();
				pinViewModel.savePin(channel, requireActivity());
			}
			UIHelper.flashMessage(requireActivity(), getResources().getString(R.string.toast_confirm_pinupdate));
			showChoiceCard(true);
		});
	}

	private void setUpRemoveAccount(Channel channel) {
		binding.removeAcct.setOnClickListener(v -> {
			new StaxDialog(requireActivity())
				.setDialogTitle(getString(R.string.removepin_dialoghead, channel.name))
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
		UIHelper.flashMessage(requireActivity(), getResources().getString(R.string.toast_confirm_acctremoved));
	}

	private void showChoiceCard(boolean show) {
		binding.choiceCard.setVisibility(show ? View.VISIBLE : View.GONE);
		binding.editCard.setVisibility(show ? View.GONE : View.VISIBLE);
		if (!show) input.requestFocus();
	}

	@Override
	public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
		Bitmap b = Bitmap.createScaledBitmap(bitmap, UIHelper.dpToPx(34), UIHelper.dpToPx(34), true);

		if(binding != null) { //wait for binding to happen when fragment is resumed before setting the image 
			RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(binding.getRoot().getContext().getResources(), b);
			d.setCircular(true);
			input.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
		}
	}

	@Override public void onBitmapFailed(Exception e, Drawable errorDrawable) {	}
	@Override public void onPrepareLoad(Drawable placeHolderDrawable) {	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		binding = null;
	}
}
