package com.hover.stax.channels;

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

import java.util.ArrayList;
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
		if(view == null) view = LayoutInflater.from(mContext).inflate(R.layout.stax_spinner_item_with_logo,parent,false);
		Channel channel = channels.get(position);
		 holder = new ViewHolder();
		 holder.logo = view.findViewById(R.id.service_item_image_id);
		 holder.channelText = view.findViewById(R.id.service_item_name_id);
		 holder.id = view.findViewById(R.id.service_item_id);
		 holder.divider = view.findViewById(R.id.service_item_divider);

		 holder.id.setText(Integer.toString(channel.id));
		 holder.channelText.setText(channel.name);
		 Picasso.get().load(channel.logoUrl).into(this);

		 if(segmentSelectedChannels) {
			 try{
				 Channel nextChannel = channels.get(position + 1);
				 if (channel.selected && !nextChannel.selected)  holder.divider.setVisibility(View.VISIBLE);
				 else holder.divider.setVisibility(View.GONE);
			 }catch (IndexOutOfBoundsException e) { holder.divider.setVisibility(View.GONE); }

		 } else holder.divider.setVisibility(View.GONE);

		return view;
	}

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
