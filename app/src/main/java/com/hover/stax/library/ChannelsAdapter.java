package com.hover.stax.library;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.databinding.LibraryListItemBinding;

import java.util.List;

class ChannelsAdapter extends RecyclerView.Adapter<ChannelsAdapter.ViewHolder> {

	private final List<Channel> channelList;
	private final LibraryListItem.DialListener dialListener;

	public ChannelsAdapter(List<Channel> channels, LibraryListItem.DialListener listener) {
		channelList = channels;
		dialListener = listener;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LibraryListItemBinding binding = LibraryListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		Channel c = channelList.get(position);
		holder.binding.liDescription.setText(c.name);
		holder.binding.liButton.setText(holder.binding.liButton.getContext().getString(R.string.library_dial_btn, c.rootCode));
		holder.binding.liButton.setOnClickListener((view) -> dialListener.dial(c.rootCode));
	}

	static class ViewHolder extends RecyclerView.ViewHolder {
		public LibraryListItemBinding binding;

		public ViewHolder(LibraryListItemBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}

	@Override
	public int getItemCount() {
		return channelList == null ? 0 : channelList.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
