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

	private MutableLiveData<List<SimInfo>> sims;

	private LiveData<List<Institution>> institutions;
	private LiveData<List<Institution>> simInstitutions;
	private LiveData<List<Institution>> countryInstitutions;
	private MutableLiveData<List<Integer>> selected = new MutableLiveData<>();

	public InstitutionViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		loadInstitutions();
		loadSims(application);
	}

	LiveData<List<Institution>> getInstitutions() { return institutions; }

	public MutableLiveData<List<SimInfo>> getSims() { return sims; }

	private void loadInstitutions() {
		if (institutions == null) {
			institutions = new MutableLiveData<>();
		}
		institutions = repo.getAll();
	}
	private void loadSims(Application application) {
		if (sims == null) {
			sims = new MutableLiveData<>();
		}
		sims.setValue(Hover.getPresentSims(application));
	}

	LiveData<List<Institution>> getCountryInstitutions(String country) {
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

	LiveData<List<Institution>> getSimInstitutions(String simHni) {
		if (simInstitutions == null) {
			simInstitutions = new MutableLiveData<>();
		}
		return simInstitutions;
	}

	public LiveData<List<Integer>> getSelected() {
		if (selected == null) {
			selected = new MutableLiveData<>();
		}
		return selected;
	}

	public void setSelected(int id) {
		List<Integer> list = selected.getValue() != null ? selected.getValue() : new ArrayList<>();
		if (list.contains(id))
			list.remove((Integer) id);
		else
			list.add(id);
		selected.setValue(list);
	}


}
