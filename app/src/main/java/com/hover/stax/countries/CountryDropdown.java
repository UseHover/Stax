package com.hover.stax.countries;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.bounties.Bounty;
import com.hover.stax.channels.Channel;
import com.hover.stax.utils.StaxFlags;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.StaxDropdownLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.yariksoffice.lingver.Lingver;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.hover.stax.utils.Constants.size55;


public class CountryDropdown extends StaxDropdownLayout implements Target {
	private static final String TAG = "CountryDropdown";
	private CountryAdapter.SelectListener selectListener;

	public CountryDropdown(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public void updateChoicesByHoverActions(List<HoverAction> actionList) {
		if (actionList == null) setEmptyState();
		else {
			Set<String> countryCodeHashset = new HashSet<>();
			for (HoverAction action : actionList) {
				countryCodeHashset.add(action.country_alpha2);
			}
			String[] codesArray = countryCodeHashset.toArray(new String[0]);
			updateChoices(codesArray);
		}
	}

	public void updateChoicesByBounties(List<Bounty> bountyList) {
		Set<String> countryCodeHashset = new HashSet<>();
		for (Bounty bounty : bountyList) {
			countryCodeHashset.add(bounty.getAction().country_alpha2);
		}
		String[] codesArray = countryCodeHashset.toArray(new String[0]);
		updateChoices(codesArray);
	}

	public void updateChoicesByChannels(List<Channel> channelList) {
		Log.d(TAG, "loading countries by channels");
		if (channelList == null) setEmptyState();
		else {
			Set<String> countryCodeHashset = new HashSet<>();
			for (Channel channel : channelList) {
				countryCodeHashset.add(channel.countryAlpha2);
			}
			String[] codesArray = countryCodeHashset.toArray(new String[0]);
			updateChoices(codesArray);
		}
	}

	public void updateChoices(String[] countryCodes) {
		Log.d(TAG, "loading countries");
		if (!hasExistingContent()) {
			CountryAdapter countryAdapter = new CountryAdapter(countryCodes, getContext());
			autoCompleteTextView.setAdapter(countryAdapter);
			autoCompleteTextView.setDropDownHeight(UIHelper.dpToPx(600));
			autoCompleteTextView.setOnItemClickListener((adapterView, view2, pos, id) -> onSelect((String) adapterView.getItemAtPosition(pos)));
		}
	}

	private boolean hasExistingContent() {
		return autoCompleteTextView.getAdapter() != null && autoCompleteTextView.getAdapter().getCount() > 0;
	}

	public void setListener(CountryAdapter.SelectListener sl) {
		selectListener = sl;
	}

	private void onSelect(String code) {
		setDropdownValue(code);
		if (selectListener != null) selectListener.countrySelect(code);
	}

	private void setEmptyState() {
		autoCompleteTextView.setDropDownHeight(0);
		setState(getContext().getString(R.string.channels_error_nodata), ERROR);
	}

	private void setDropdownValue(String value) {
		if (value != null) setCountryTextAndFlag(autoCompleteTextView, value);
	}

	private void setCountryTextAndFlag(AutoCompleteTextView tv, String code) {
			int countryRes = StaxFlags.getResId(getContext(), code);
			UIHelper.loadPicasso(countryRes, size55, this);
			tv.setText(getFullCountryName(code), false);
	}
	public static String getFullCountryName(String code){
		Locale loc = new Locale(Lingver.getInstance().getLanguage(), code);
		return loc.getDisplayCountry();
	}

	@Override
	public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
		RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create (getContext().getResources(), bitmap);
		autoCompleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null);
	}

	@Override
	public void onBitmapFailed(Exception e, Drawable errorDrawable) {

	}

	@Override
	public void onPrepareLoad(Drawable placeHolderDrawable) {

	}
}
