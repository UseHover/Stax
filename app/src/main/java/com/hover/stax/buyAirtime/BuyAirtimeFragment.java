package com.hover.stax.buyAirtime;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hover.stax.R;

public class BuyAirtimeFragment extends Fragment {

	private BuyAirtimeViewModel buyAirtimeViewModel;

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		buyAirtimeViewModel = new ViewModelProvider(this).get(BuyAirtimeViewModel.class);
		View root = inflater.inflate(R.layout.fragment_buyairtime, container, false);
		AppCompatSpinner spinnerTo = root.findViewById(R.id.toSpinner);

		AppCompatSpinner spinnerFrom = root.findViewById(R.id.fromSpinner);

		spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				AppCompatTextView textView = (AppCompatTextView) parent.getChildAt(0);
				if (textView != null) {
					textView.setTextColor(getResources().getColor(R.color.white));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});


		TextView recipientLabel = root.findViewById(R.id.airtime_recipientLabel);
		EditText recipientEdit = root.findViewById(R.id.airtimeToEditId);

		spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				AppCompatTextView textView = (AppCompatTextView) parent.getChildAt(0);
				if (textView != null) {
					textView.setTextColor(getResources().getColor(R.color.white));
				}

				if(position > 0) {
					recipientEdit.setVisibility(View.VISIBLE);
					recipientLabel.setVisibility(View.VISIBLE);
				}
				else {
					recipientEdit.setVisibility(View.GONE);
					recipientLabel.setVisibility(View.GONE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		return root;
	}
}
