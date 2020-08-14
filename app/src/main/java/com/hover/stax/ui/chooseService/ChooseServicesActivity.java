package com.hover.stax.ui.chooseService;

import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.adapters.ChooseServiceViewModel;
import com.hover.stax.adapters.ChooseServicesAdapters;
import com.hover.stax.enums.Service_category;
import com.hover.stax.enums.Service_in_list_status;
import com.hover.stax.interfaces.CustomOnClickListener;
import com.hover.stax.repo.DataRepo;
import com.hover.stax.utils.UIHelper;

public class ChooseServicesActivity extends AppCompatActivity implements CustomOnClickListener {
private ChooseServicesAdapters servicesAdapters_yourSim;
private ChooseServicesAdapters servicesAdapters_inYourCountry;
ChooseServicesAdapters servicesAdapters_allServices;

@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.choose_services);

	TextView otherServicesText = findViewById(R.id.other_services_in);
	TextView doneText = findViewById(R.id.choose_serves_done);
	otherServicesText.append(" "+ new DataRepo().getSimCountry());

	int textColorAdded = getResources().getColor(R.color.mediumGrey);
	int textColorNotAdded = getResources().getColor(R.color.colorWhiteV2);

	UIHelper.setTextUnderline(doneText, getResources().getString(R.string.done));
	doneText.setOnClickListener(view->finish());


	ChooseServiceViewModel serviceViewModel = new ViewModelProvider(this).get(ChooseServiceViewModel.class);
	serviceViewModel.getServicesBasedOnSim_liveData();
	serviceViewModel.getServicesBasedOnCountry_liveData();
	serviceViewModel.getAllServices_liveData();

	serviceViewModel.loadServicesBasedOnSim().observe(this, staxServicesModels -> {
		RecyclerView servicesBySimRecyclerView = findViewById(R.id.choose_service_recycler_yourSIMS);
		GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
		servicesBySimRecyclerView.setHasFixedSize(true);
		servicesBySimRecyclerView.setLayoutManager(gridLayoutManager);


		servicesAdapters_yourSim = new ChooseServicesAdapters(staxServicesModels, this, Service_category.YOUR_SIM, textColorAdded, textColorNotAdded);
		servicesBySimRecyclerView.setAdapter(servicesAdapters_yourSim);
	});

	serviceViewModel.loadServicesBasedOnCountry().observe(this, staxServicesModels -> {
		RecyclerView servicesByCountryRecyclerView = findViewById(R.id.choose_service_recycler_inCountry);
		GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
		servicesByCountryRecyclerView.setHasFixedSize(true);
		servicesByCountryRecyclerView.setLayoutManager(gridLayoutManager);

		servicesAdapters_inYourCountry = new ChooseServicesAdapters(staxServicesModels, this, Service_category.IN_COUNTRY, textColorAdded, textColorNotAdded);
		servicesByCountryRecyclerView.setAdapter(servicesAdapters_inYourCountry);
	});

	serviceViewModel.loadAllServices().observe(this, staxServicesModels -> {
		RecyclerView allServicesRecyclerView = findViewById(R.id.choose_service_recycler_allservices);
		GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
		allServicesRecyclerView.setHasFixedSize(true);
		allServicesRecyclerView.setLayoutManager(gridLayoutManager);

		servicesAdapters_allServices = new ChooseServicesAdapters(staxServicesModels, this, Service_category.ALL_SERVICES, textColorAdded, textColorNotAdded);
		allServicesRecyclerView.setAdapter(servicesAdapters_allServices);
	});


}

@Override
public void customClickListener(Object... data) {
	String serviceId = (String )data[0];
	Service_in_list_status newStatus = (Service_in_list_status) data[1];
	int position = (int) data[2];
	Service_category category = (Service_category) data[3];

	if(category == Service_category.YOUR_SIM) {
		servicesAdapters_yourSim.updateSelectStatus(newStatus, position);

		boolean tapRequestStatus = new DataRepo().addServiceToUserCatalogue(newStatus, serviceId);
		if(!tapRequestStatus) servicesAdapters_yourSim.resetSelectStatus(position);

	}
	else if(category == Service_category.IN_COUNTRY) {
		servicesAdapters_inYourCountry.updateSelectStatus(newStatus, position);

		boolean tapRequestStatus = new DataRepo().addServiceToUserCatalogue(newStatus, serviceId);
		if(!tapRequestStatus) servicesAdapters_inYourCountry.resetSelectStatus(position);
	}
	else {
		servicesAdapters_allServices.updateSelectStatus(newStatus, position);

		boolean tapRequestStatus = new DataRepo().addServiceToUserCatalogue(newStatus, serviceId);
		if(!tapRequestStatus) servicesAdapters_allServices.resetSelectStatus(position);
	}
}
}
