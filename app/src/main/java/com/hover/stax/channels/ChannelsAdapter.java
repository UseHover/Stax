package com.hover.stax.channels;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_service_item, parent, false);
		return new ChannelViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
		Channel channel = channels.get(position);

		holder.id.setText(Integer.toString(channel.id));
		holder.name.setText(channel.name + " " + channel.countryAlpha2);
		//holder.serviceLogo.setImageBitmap(channel.logo);

		holder.shadowFrame.setVisibility(selected.contains(channel.id) || channel.selected ? View.VISIBLE : View.GONE);
		holder.checkIcon.setVisibility(selected.contains(channel.id) || channel.selected ? View.VISIBLE : View.GONE);

	}

	class ChannelViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		TextView name, id;
		CircleImageView logo, shadowFrame;
		ImageView checkIcon;

		ChannelViewHolder(@NonNull View itemView) {
			super(itemView);
			id = itemView.findViewById(R.id.service_item_id);
			name = itemView.findViewById(R.id.service_item_name_id);
			logo = itemView.findViewById(R.id.service_item_image_id);
			shadowFrame = itemView.findViewById(R.id.checked_service_frame);
			checkIcon = itemView.findViewById(R.id.checked_service_tick);

			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			selectListener.onTap(Integer.parseInt(id.getText().toString()));
		}
	}

	public interface SelectListener  {
		void onTap(int channelId);
	}

	@Override public int getItemCount() { return channels == null ? 0 : channels.size(); }

	@Override public long getItemId(int position) {
		return position;
	}

	@Override public int getItemViewType(int position) {
		return position;
	}
}
