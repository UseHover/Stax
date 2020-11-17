package com.hover.stax.channels;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class ChannelsAdapter extends RecyclerView.Adapter<ChannelsAdapter.ChannelViewHolder> {
	private List<Channel> channels;
	private List<Integer> selected;

	private final SelectListener selectListener;

	public ChannelsAdapter(List<Channel> channelList, SelectListener listener) {
		this.channels = channelList;
		this.selectListener = listener;
		selected = new ArrayList<>();
	}

	public void updateSelected(List<Integer> ids) {
		selected = ids;
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_channel_item, parent, false);
		return new ChannelViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
		Channel channel = channels.get(position);
		holder.id.setText(Integer.toString(channel.id));
		holder.name.setText(channel.name + " " + channel.countryAlpha2);
		Picasso.get().load(channel.logoUrl).into(holder);
		holder.logoWrapper.setForeground(selected.contains(channel.id) ?
			holder.itemView.getResources().getDrawable(R.drawable.ic_grey_circle) : null);
		holder.checkIcon.setVisibility(selected.contains(channel.id) ? View.VISIBLE : View.GONE);
	}

	class ChannelViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, Target {
		TextView name, id;
		ImageView logo;
		FrameLayout logoWrapper;
		ImageView checkIcon;

		ChannelViewHolder(@NonNull View itemView) {
			super(itemView);
			id = itemView.findViewById(R.id.service_item_id);
			name = itemView.findViewById(R.id.service_item_name_id);
			logo = itemView.findViewById(R.id.service_item_image_id);
			logoWrapper = itemView.findViewById(R.id.img_wrapper);
			checkIcon = itemView.findViewById(R.id.checked_service_tick);

			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if (selectListener != null)
				selectListener.onTap(Integer.parseInt(id.getText().toString()));
		}

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(itemView.getContext().getResources(), bitmap);
			d.setCircular(true);
			logo.setImageDrawable(d);
		}

		@Override
		public void onBitmapFailed(Exception e, Drawable errorDrawable) {
			Log.e("LogTag", e.getMessage());
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
		}
	}

	public interface SelectListener {
		void onTap(int channelId);
	}

	@Override
	public int getItemCount() {
		return channels == null ? 0 : channels.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}
}
