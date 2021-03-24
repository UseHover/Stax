package com.hover.stax.countries;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.hover.stax.R;
import com.hover.stax.utils.UIHelper;

public class CountryAdapter extends ArrayAdapter<String> {
	private static final String TAG ="CountryAdapter";
	private String[] countryCodes;

	public CountryAdapter(@NonNull  String[] countryCodes, @NonNull Context context) {
		super(context, 0, countryCodes);
		this.countryCodes = countryCodes;

	}

	@SuppressLint("ViewHolder")
	@NonNull
	@Override
	public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
		view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.country_item, parent, false);
		String code = countryCodes[position];
		CountryViewHolder holder = new CountryViewHolder(view);
		holder.setTextAndFlag(code);
		return view;
	}

	@Override
	public int getCount() {
		return countryCodes.length;
	}

	@Override
	public long getItemId(int position) { return position; }
	@Override
	public int getItemViewType(int position) {
		return position;
	}

	public interface SelectListener {
		void countrySelect(String countryCode);
	}

	static private class CountryViewHolder {
		private AppCompatTextView textView;
		public CountryViewHolder(@NonNull View itemView) {
			textView = itemView.findViewById(R.id.country_text_id);
		}
		void setTextAndFlag(String code) {
			String countryWithEmoji = textView.getContext().getString(R.string.country_with_emoji, UIHelper.countryCodeToEmoji(code),CountryDropdown.getFullCountryName(code) );
			textView.setText(countryWithEmoji);
		}
	}
}
