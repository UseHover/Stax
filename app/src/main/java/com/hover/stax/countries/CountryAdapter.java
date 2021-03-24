package com.hover.stax.countries;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.hover.stax.R;
import com.hover.stax.utils.StaxFlags;
import com.hover.stax.utils.UIHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import static com.hover.stax.utils.Constants.size55;

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

	static private class CountryViewHolder implements Target {
		private AppCompatTextView textView;
		public CountryViewHolder(@NonNull View itemView) {
			textView = itemView.findViewById(R.id.country_text_id);
		}
		void setTextAndFlag(String code) {
			textView.setText(CountryDropdown.getFullCountryName(code));
			UIHelper.loadPicasso(StaxFlags.getResId(textView.getContext(), code), size55, this);
		}

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create (textView.getContext().getResources(), bitmap);
			textView.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null);
		}

		@Override
		public void onBitmapFailed(Exception e, Drawable errorDrawable) {

		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {

		}
	}
}
