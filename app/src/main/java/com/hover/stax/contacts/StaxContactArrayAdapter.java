package com.hover.stax.contacts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hover.stax.R;

import java.util.List;

public class StaxContactArrayAdapter extends ArrayAdapter<StaxContact> {

	private List<StaxContact> contacts;

	public StaxContactArrayAdapter(@NonNull Context context, List<StaxContact> list) {
		super(context, 0 , list);
		contacts = list;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View v, @NonNull ViewGroup parent) {
		if (v == null)
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stax_spinner_2line, parent, false);

		StaxContact c = contacts.get(position);

		((TextView) v.findViewById(R.id.title)).setText(c.shortName(false));
		((TextView) v.findViewById(R.id.subtitle)).setText(c.getPhoneNumber());
		v.findViewById(R.id.subtitle).setVisibility(c.hasName() ? View.VISIBLE : View.GONE);

		return v;
	}
}