package com.hover.stax.countries;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.StaxDropdownLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CountryDropdown extends StaxDropdownLayout {

    CountryAdapter adapter;
    private CountryAdapter.SelectListener selectListener;

    public CountryDropdown(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void updateChoices(List<Channel> channels, String currentCountry) {
        if (channels == null || channels.size() == 0) {
            setEmptyState();
            return;
        }
        adapter = new CountryAdapter(getCountryCodes(channels), getContext());
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setDropDownHeight(UIHelper.dpToPx(600));
        autoCompleteTextView.setOnItemClickListener((adapterView, view2, pos, id) -> onSelect((String) adapterView.getItemAtPosition(pos)));
        setDropdownValue(currentCountry);
        adapter.notifyDataSetChanged();
    }

    private String[] getCountryCodes(List<Channel> channelList) {
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

    public void setDropdownValue(String countryCode) {
        if (countryCode == null)
            countryCode = CountryAdapter.codeRepresentingAllCountries();

        if (adapter != null)
            autoCompleteTextView.setText(adapter.getCountryString(countryCode));
    }
}
