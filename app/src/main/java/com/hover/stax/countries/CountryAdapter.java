package com.hover.stax.countries;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hover.stax.R;
import com.yariksoffice.lingver.Lingver;

import java.util.Locale;

public class CountryAdapter extends ArrayAdapter<String> {
	private static final String TAG ="CountryAdapter";

	private String[] countryCodes;

	public CountryAdapter(@NonNull  String[] countryCodes, @NonNull Context context) {
		super(context, 0, countryCodes);
		this.countryCodes = countryCodes;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
		if (view == null)
			view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.country_item, parent, false);
		((TextView) view.findViewById(R.id.country_text_id)).setText(getCountryString(countryCodes[position]));
		return view;
	}

	String getCountryString(String code) {
		return getContext().getString(R.string.country_with_emoji,
			countryCodeToEmoji(code), getFullCountryName(code));
	}

	private static String getFullCountryName(String code){
		Locale loc = new Locale(Lingver.getInstance().getLanguage(), code);
		return loc.getDisplayCountry();
	}

	private static String countryCodeToEmoji(String countryCode) {
		int firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6;
		int secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6;
		return new String(Character.toChars(firstLetter)) + new String(Character.toChars(secondLetter));
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
}
