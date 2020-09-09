package com.hover.stax.actions;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hover.stax.R;

import java.util.List;

public class ActionAdapter extends ArrayAdapter<Action> {

	LayoutInflater inflater;

	public ActionAdapter(@NonNull Activity c, int resource, @NonNull List objects) {
		super(c, resource, objects);
		inflater = c.getLayoutInflater();
	}

	@Override
	public View getDropDownView(int position, View view, ViewGroup viewGroup) {
		Action action = getItem(position);
		if (action == null) return null;
		View row = inflater.inflate(R.layout.spinner_items, null, true);
		((TextView) row).setText(action.toString());
		return row;
	}
}
