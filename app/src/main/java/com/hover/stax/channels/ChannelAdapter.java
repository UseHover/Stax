package com.hover.stax.channels;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hover.stax.R;

import java.util.List;

public class ChannelAdapter extends ArrayAdapter<Channel> {

	LayoutInflater inflater;

	public ChannelAdapter(@NonNull Activity context, int resource, @NonNull List objects) {
		super(context, resource, objects);
		inflater = context.getLayoutInflater();
	}

	@Override
	public View getDropDownView(int position, View view, ViewGroup viewGroup) {
		Channel channel = getItem(position);
		if (channel == null) return null;
		View row = inflater.inflate(R.layout.spinner_items, null, true);
		((TextView) row).setText(channel.name);
		return row;
	}
}
