package com.hover.stax.institutions;

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

public class InstitutionsActivity extends AppCompatActivity implements InstitutionsAdapter.SelectListener {
	public final static String TAG = "ChooseServicesActivity";

	InstitutionViewModel instViewModel;
	List<String> countryList;
	List<String> hniList;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!PermissionUtils.hasPhonePerm(this))
			PermissionUtils.requestPhonePerms(this, 0);

		setContentView(R.layout.choose_services);
		findViewById(R.id.choose_serves_done).setOnClickListener(view -> startActivity(new Intent(this, ServicesPinActivity.class)));

		instViewModel = new ViewModelProvider(this).get(InstitutionViewModel.class);

		getHnis();
		getCountries();
		addInstitutions();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (PermissionUtils.permissionsGranted(grantResults)) {
			Hover.updateSimInfo(this);
		}
	}

	private void getCountries() {
		instViewModel.getSims().observe(this, sims -> {
			if (countryList == null) countryList = new ArrayList<>();
			for (SimInfo sim: sims) {
				if (!countryList.contains(sim.getCountryIso()))
					countryList.add(sim.getCountryIso());
			}
			if (countryList.size() > 0)
				((TextView) findViewById(R.id.other_services_in)).setText(getString(R.string.country_section, countryList.get(0)));
		});
	}

	private void getHnis() {
		List<SimInfo> sims = instViewModel.getSims().getValue();
		if (hniList == null) hniList = new ArrayList<>();
		for (SimInfo sim: sims) {
			if (!hniList.contains(sim.getOSReportedHni()))
				hniList.add(sim.getOSReportedHni());
		}
	}

	private void addInstitutions() {
		instViewModel.getSimInstitutions("63902").observe(this, institutions -> {
			addGrid(findViewById(R.id.choose_service_recycler_yourSIMS), institutions);
		});

		instViewModel.getCountryInstitutions("ke").observe(this, institutions -> {
			addGrid(findViewById(R.id.choose_service_recycler_inCountry), institutions);
		});

		instViewModel.getInstitutions().observe(this, institutions -> {
			addGrid(findViewById(R.id.choose_service_recycler_allservices), institutions);
		});
	}

	private void addGrid(RecyclerView view, List<Institution> institutions) {
		GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
		view.setHasFixedSize(true);
		view.setLayoutManager(gridLayoutManager);
		InstitutionsAdapter instAdapter = new InstitutionsAdapter(institutions, this);
		view.setAdapter(instAdapter);
		instViewModel.getSelected().observe(this, instAdapter::updateSelected);
	}

	public void onTap(int id) {
		instViewModel.setSelected(id);
	}
}
