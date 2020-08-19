package com.hover.stax.institutions;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.sdk.api.Hover;
import com.hover.sdk.sims.SimInfo;
import com.hover.stax.database.DatabaseRepo;

import java.util.ArrayList;
import java.util.List;

public class InstitutionViewModel extends AndroidViewModel {

	private DatabaseRepo repo;

	private List<SimInfo> sims;

	private LiveData<List<Institution>> institutions;
	private LiveData<List<Institution>> simInstitutions;
	private LiveData<List<Institution>> countryInstitutions;
	private final MutableLiveData<List<Institution>> selected = new MutableLiveData<>();

	public InstitutionViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		institutions = repo.getAll();
		sims = Hover.getPresentSims(application);
	}

	public LiveData<List<Institution>> getInstitutions() {
		if (institutions == null) {
			institutions = new MutableLiveData<>();
			loadInstitutions();
		}
		return institutions;
	}

	private void loadInstitutions() {
		institutions = repo.getAll();
	}

	public LiveData<List<Institution>> getCountryInstitutions(String country) {
		if (countryInstitutions == null) {
			countryInstitutions = new MutableLiveData<>();
		}
		if (institutions == null) {
			institutions = new MutableLiveData<>();
			loadInstitutions();
		}
		List<Institution> countryInsts = institutions.getValue();
		List<Institution> fresh = new ArrayList<>();

//		for (int i = 0; i < countryInsts.size(); i++) {
//			if (countryInsts.get(i).countryAlpha2.equals(country))
//				fresh.add(countryInsts.get(i));
//		}
//		countryInstitutions.setValue(fresh);
		return countryInstitutions;
	}

	public LiveData<List<Institution>> getSimInstitutions(String simHni) {
		if (simInstitutions == null) {
			simInstitutions = new MutableLiveData<>();
		}
		return simInstitutions;
	}

	public List<SimInfo> getSims() { return sims; }

	public LiveData<List<Institution>> getSelected() {
		return selected;
	}

	public void select(Institution inst) {
		List<Institution> list = selected.getValue();
		list.add(inst);
		selected.setValue(list);
	}


}
