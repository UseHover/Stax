package com.hover.stax.ui.moveMoney.betweenServices;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hover.stax.R;
import com.hover.stax.models.BetweenServicesDataModel;
import com.hover.stax.models.StaxGetServiceAndActionModel;
import com.hover.stax.ui.home.HomeViewModel;
import com.hover.stax.utils.UIHelper;

import java.util.ArrayList;

public class BetweenServicesActivity extends AppCompatActivity {

	private AppCompatSpinner fromSpinner, toSpinner;
	private int fromIndex = 0;
	private String selectedAction;
	private ArrayList<BetweenServicesDataModel> betweenServicesDataModelArrayList;
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.between_services_layout);

	fromSpinner = findViewById(R.id.bs_from);
	toSpinner = findViewById(R.id.bs_to);


	BetweenServicesViewModel betweenServicesViewModel = new ViewModelProvider(this).get(BetweenServicesViewModel.class);
	betweenServicesViewModel.getBetweenServicesData();

	betweenServicesViewModel.loadBetweenServicesData().observe(this, betweenServicesDataModels -> {
		ArrayList<String> fromStrings = new ArrayList<>();
		betweenServicesDataModelArrayList = betweenServicesDataModels;
		for(BetweenServicesDataModel bsd : betweenServicesDataModelArrayList) {
			fromStrings.add(bsd.getServiceName());
		}
		UIHelper.loadSpinnerItems(fromStrings, fromSpinner, BetweenServicesActivity.this);
		fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				fromIndex = position;
				updateTheToSpinner();
				UIHelper.updateSpinnerTextColor(parent, BetweenServicesActivity.this);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	});


}

public void updateTheToSpinner() {
	ArrayList<String> toStrings = new ArrayList<>();
	for(StaxGetServiceAndActionModel sgt: betweenServicesDataModelArrayList.get(fromIndex).getStaxGetServiceAndActionModel()) {
		toStrings.add(sgt.getServiceName());
	}
	UIHelper.loadSpinnerItems(toStrings, toSpinner, this);
	toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			selectedAction = betweenServicesDataModelArrayList.get(fromIndex).getStaxGetServiceAndActionModel().get(position).getActionIdLinkingBothServices();
			UIHelper.updateSpinnerTextColor(parent, BetweenServicesActivity.this);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}
	});

}
}
