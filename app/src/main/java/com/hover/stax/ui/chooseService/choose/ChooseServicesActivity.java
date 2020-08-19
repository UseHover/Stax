package com.hover.stax.ui.chooseService.choose;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.sdk.api.Hover;
import com.hover.sdk.sims.SimInfo;
import com.hover.stax.R;
import com.hover.stax.adapters.InstitutionsAdapter;
import com.hover.stax.institutions.Institution;
import com.hover.stax.institutions.InstitutionViewModel;
import com.hover.stax.database.ConvertRawDatabaseDataToModels;
import com.hover.stax.ui.chooseService.pin.ServicesPinActivity;
import com.hover.stax.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseServicesActivity extends AppCompatActivity implements InstitutionsAdapter.SelectListener {

	List<String> countryList;
	List<String> hniList;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!PermissionUtils.hasPhonePerm(this))
			PermissionUtils.requestPhonePerms(this, 0);

		setContentView(R.layout.choose_services);
		findViewById(R.id.choose_serves_done).setOnClickListener(view -> startActivity(new Intent(this, ServicesPinActivity.class)));

		InstitutionViewModel viewModel = new ViewModelProvider(this).get(InstitutionViewModel.class);
		((TextView) findViewById(R.id.other_services_in))
			.append(" " + new ConvertRawDatabaseDataToModels().getSimCountry());

		getHnis(viewModel);
		getCountries(viewModel);
		addInstitutions(viewModel);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (PermissionUtils.permissionsGranted(grantResults)) {
			Hover.updateSimInfo(this);
		}
	}

	private void getCountries(InstitutionViewModel viewModel) {
		List<SimInfo> sims = viewModel.getSims();
		if (countryList == null) countryList = new ArrayList<>();
		for (SimInfo sim: sims) {
			if (!countryList.contains(sim.getCountryIso()))
				countryList.add(sim.getCountryIso());
		}
	}

	private void getHnis(InstitutionViewModel viewModel) {
		List<SimInfo> sims = viewModel.getSims();
		if (hniList == null) hniList = new ArrayList<>();
		for (SimInfo sim: sims) {
			if (!hniList.contains(sim.getOSReportedHni()))
				hniList.add(sim.getOSReportedHni());
		}
	}

	private void addInstitutions(InstitutionViewModel viewModel) {
		viewModel.getSimInstitutions("63902").observe(this, institutions -> {
			addGrid(findViewById(R.id.choose_service_recycler_yourSIMS), institutions);
		});

		viewModel.getCountryInstitutions("ke").observe(this, institutions -> {
			addGrid(findViewById(R.id.choose_service_recycler_inCountry), institutions);
		});

		viewModel.getInstitutions().observe(this, institutions -> {
			addGrid(findViewById(R.id.choose_service_recycler_allservices), institutions);
		});
	}

	private void addGrid(RecyclerView view, List<Institution> institutions) {
		GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
		view.setHasFixedSize(true);
		view.setLayoutManager(gridLayoutManager);
		view.setAdapter(new InstitutionsAdapter(institutions, new ArrayList<>(), this));
	}

	public void onSelect(int id) {
		Log.e(TAG, "Not error! It clicked.");
	}
}
