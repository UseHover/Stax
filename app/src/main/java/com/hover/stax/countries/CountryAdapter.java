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
import com.hover.stax.databinding.CountryItemBinding;
import com.yariksoffice.lingver.Lingver;

import java.util.Locale;

public class CountryAdapter extends ArrayAdapter<String> {
	private static final String TAG ="CountryAdapter";

	private String[] countryCodes;

	public CountryAdapter(@NonNull  String[] codes, @NonNull Context context) {
		super(context, 0, codes);
		this.countryCodes = codes;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
		ViewHolder holder;

		if (view == null) {
			CountryItemBinding binding = CountryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
			view = binding.getRoot();

			holder = new ViewHolder(binding);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.countryText.setText(getCountryString(countryCodes[position]));

		return view;
	}

	private static class ViewHolder {
		TextView countryText;

		public ViewHolder(CountryItemBinding binding){
			countryText = binding.countryTextId;
		}
	}

	String getCountryString(String code) {
		if(code.equals(codeRepresentingAllCountries())) return getContext().getString(R.string.all_countries_with_emoji);
		return getContext().getString(R.string.country_with_emoji, countryCodeToEmoji(code), getFullCountryName(code));
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
	public static String codeRepresentingAllCountries(){
		return "00";
	}

	@Nullable
	@Override
	public String getItem(int position) {
		if (getCount() > 0) return countryCodes[position];
		else return null;
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
