package com.hover.stax.countries;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.bounties.Bounty;
import com.hover.stax.channels.Channel;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.StaxDropdownLayout;
import com.yariksoffice.lingver.Lingver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CountryDropdown extends StaxDropdownLayout {
	private static final String TAG = "CountryDropdown";

	CountryAdapter adapter;
	private CountryAdapter.SelectListener selectListener;

	public CountryDropdown(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public void updateChoices(List<Channel> channels) {
		Log.d(TAG, "loading countries");
		if (channels == null || channels.size() == 0) {
			setEmptyState();
			return;
		}
		adapter = new CountryAdapter(getCountryCodes(channels), getContext());
		autoCompleteTextView.setAdapter(adapter);
		autoCompleteTextView.setDropDownHeight(UIHelper.dpToPx(600));
		autoCompleteTextView.setOnItemClickListener((adapterView, view2, pos, id) -> onSelect((String) adapterView.getItemAtPosition(pos)));
	}

	private String[] getCountryCodes(List<Channel> channelList) {
		Log.d(TAG, "loading countries by channels");
		List<String> countryCodes = new ArrayList<>();

		countryCodes.add(CountryAdapter.codeRepresentingAllCountries());
		for (Channel channel : channelList) {
			if (!countryCodes.contains(channel.countryAlpha2))
				countryCodes.add(channel.countryAlpha2);
		}
		Collections.sort(countryCodes);
		return countryCodes.toArray(new String[0]);
	}


	private void setEmptyState() {
		autoCompleteTextView.setDropDownHeight(0);
		setState(getContext().getString(R.string.channels_error_nodata), ERROR);
	}

	public void setListener(CountryAdapter.SelectListener sl) {
		selectListener = sl;
	}

	private void onSelect(String code) {
		setDropdownValue(code);
		if (selectListener != null) selectListener.countrySelect(code);
	}

	private void setDropdownValue(String countryCode) {
		if (countryCode != null && adapter != null)
			autoCompleteTextView.setText(adapter.getCountryString(countryCode));
	}
}
