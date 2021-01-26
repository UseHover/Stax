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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public class ChannelDropdownAdapter extends ArrayAdapter<Channel> implements Target {
	private List<Channel> channels;
	private final Context mContext;
	private boolean segmentSelectedChannels = false;
	private ViewHolder holder;

	public ChannelDropdownAdapter(@NonNull List<Channel> channels, boolean segmentSelectedChannels,  @NonNull Context context) {
		super(context, 0, channels);
		this.mContext = context;
		this.channels = channels;
		this.segmentSelectedChannels = segmentSelectedChannels;
	}

	@Override
	public int getCount() { return channels.size();
	}
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		View view = convertView;
		if (view == null) view = LayoutInflater.from(mContext).inflate(R.layout.stax_spinner_item_with_logo, parent,false);
		Channel channel = channels.get(position);

		initItemViews(view);
		setViewData(channel);
		segmentSelectedChannelsIfNeeded(channel, position);

		return view;
	}

	private  void initItemViews(View view) {
		holder = new ViewHolder();
		holder.logo = view.findViewById(R.id.service_item_image_id);
		holder.channelText = view.findViewById(R.id.service_item_name_id);
		holder.id = view.findViewById(R.id.service_item_id);
		holder.divider = view.findViewById(R.id.service_item_divider);
	}

	@SuppressLint("SetTextI18n")
	private void setViewData(Channel channel) {
		holder.id.setText(Integer.toString(channel.id));
		holder.channelText.setText(channel.name);
		Picasso.get().load(channel.logoUrl).into(this);
	}

	private void segmentSelectedChannelsIfNeeded(Channel currentChannel, int pos) {
		if (segmentSelectedChannels) {
			try{
				Channel nextChannel = channels.get(pos + 1);
				if (currentChannel.selected && !nextChannel.selected) addDivider();
				else removeDivider();
			}catch (IndexOutOfBoundsException e) { removeDivider(); }
		} else removeDivider();
	}

	private void addDivider() {holder.divider.setVisibility(View.VISIBLE);}
	private void removeDivider() { holder.divider.setVisibility(View.GONE); }

	private static class ViewHolder {
		TextView id;
		ImageView logo;
		AppCompatTextView channelText;
		View divider;
	}

	@Override
	public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
		RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(mContext.getResources(), bitmap);
		d.setCircular(true);
		holder.logo.setImageDrawable(d);
	}

	@Override
	public void onBitmapFailed(Exception e, Drawable errorDrawable) {
		Log.e("LogTag", e.getMessage());
	}

	@Override
	public void onPrepareLoad(Drawable placeHolderDrawable) {

	}
}
