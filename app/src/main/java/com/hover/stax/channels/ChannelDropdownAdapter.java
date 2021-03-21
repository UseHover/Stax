package com.hover.stax.channels;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.hover.stax.R;
import com.hover.stax.utils.UIHelper;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.hover.stax.utils.Constants.size55;

public class ChannelDropdownAdapter extends ArrayAdapter<Channel> {
	private List<Channel> channels;
	private ViewHolder holder;

	public ChannelDropdownAdapter(@NonNull List<Channel> channelList, @NonNull Context context) {
		super(context, 0, channelList);
		channels = channelList;
	}

	public static List<Channel> sort(List<Channel> channels, boolean showSelected) {
		ArrayList<Channel> selected_list = new ArrayList<>();
		ArrayList<Channel> sorted_list = new ArrayList<>();
		for (Channel c : channels) {
			if (c.selected) selected_list.add(c);
			else sorted_list.add(c);
		}
		Collections.sort(selected_list);
		Collections.sort(sorted_list);
		if (showSelected)
			sorted_list.addAll(0, selected_list);
		return sorted_list;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
		Channel c = channels.get(position);
		Log.e("ADAPTER", "getting view for pos " + position);
		view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stax_spinner_item_with_logo, parent,false);

		holder = new ViewHolder(view);
		holder.setChannel(c);
		updateDivider(c, position);

		return view;
	}

	private void updateDivider(Channel currentChannel, int pos) {
		if (pos > 0) {
			Channel prevChannel = channels.get(pos - 1);
			if (!currentChannel.selected && prevChannel.selected) addDivider();
			else removeDivider();
		} else removeDivider();
	}

	private void addDivider() { holder.divider.setVisibility(View.VISIBLE); }
	private void removeDivider() { holder.divider.setVisibility(View.GONE); }

	private static class ViewHolder implements Target {
		TextView id;
		ImageView logo;
		AppCompatTextView channelText;
		View divider;

		private ViewHolder(View view) {
			logo = view.findViewById(R.id.service_item_image_id);
			channelText = view.findViewById(R.id.service_item_name_id);
			id = view.findViewById(R.id.service_item_id);
			divider = view.findViewById(R.id.service_item_divider);
		}

		@SuppressLint("SetTextI18n")
		private void setChannel(Channel channel) {
			id.setText(Integer.toString(channel.id));
			channelText.setText(channel.toString());
			UIHelper.loadPicasso(channel.logoUrl, size55, this);
		}

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(id.getContext().getResources(), bitmap);
			d.setCircular(true);
			logo.setImageDrawable(d);
		}

		@Override
		public void onBitmapFailed(Exception e, Drawable errorDrawable) {
			Log.e("LogTag", e.getMessage());
		}

		@Override public void onPrepareLoad(Drawable placeHolderDrawable) { }
	}

	@Override
	public int getCount() { return channels.size(); }

	@Override
	public long getItemId(int position) { return position; }

	@Override
	public int getItemViewType(int position) {
		return position;
	}
}
