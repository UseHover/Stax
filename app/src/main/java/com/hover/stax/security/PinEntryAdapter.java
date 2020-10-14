package com.hover.stax.security;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.utils.UIHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public class PinEntryAdapter extends RecyclerView.Adapter<PinEntryAdapter.PinEntryViewHolder> {
	private List<Channel> channels;
	private UpdateListener updateListener;

	PinEntryAdapter(List<Channel> channels, UpdateListener listener) {
		this.channels = channels;
		this.updateListener = listener;
	}

	@NonNull
	@Override
	public PinEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pin_entry_item, parent, false);
		return new PinEntryViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final @NonNull PinEntryViewHolder holder, int position) {
		Channel channel = channels.get(position);

		holder.view.setTag(channel.id);
		holder.label.setHint(channel.name);
		Picasso.get().load(channel.logoUrl).into (holder);

		holder.input.addTextChangedListener(new TextWatcher() {
			@Override public void afterTextChanged(Editable s) { }
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				updateListener.onUpdate(channel.id, s.toString());
			}
		});

		if (channel.pin != null && !channel.pin.isEmpty()) {
			holder.input.setText(KeyStoreExecutor.decrypt(channel.pin, holder.view.getContext()));
		}
	}

	public interface UpdateListener {
		void onUpdate(int id, String pin);
	}

	static class PinEntryViewHolder extends RecyclerView.ViewHolder implements Target {
		final View view;
		final TextInputLayout label;
		final TextInputEditText input;

		PinEntryViewHolder(@NonNull View itemView) {
			super(itemView);
			view = itemView;
			label = itemView.findViewById(R.id.pinEntry);
			input = itemView.findViewById(R.id.pin_input);
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
			Log.e("LogTag", e.getMessage());
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) { }
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return channels == null ? 0 : channels.size();
	}
}

