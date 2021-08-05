package com.hover.stax.library;

import static org.koin.java.KoinJavaComponent.get;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.channels.Channel;
import com.hover.stax.countries.CountryAdapter;
import com.hover.stax.database.DatabaseRepo;

import java.util.List;

import timber.log.Timber;

public class LibraryViewModel extends AndroidViewModel {
	private static String TAG = "LibraryViewModel";

	private DatabaseRepo repo;

	private LiveData<List<Channel>> allChannels;
	private MutableLiveData<List<Channel>> filteredChannels = new MutableLiveData<>();

	public LibraryViewModel(@NonNull Application application) {
		super(application);
		repo = get(DatabaseRepo.class);
		allChannels = repo.getAllChannels();
		filteredChannels.setValue(allChannels.getValue());
	}

	public void filterChannels(String countryCode) {
		new Thread(() -> {
			if (countryCode.equals(CountryAdapter.codeRepresentingAllCountries()))
				filteredChannels.postValue(allChannels.getValue());
			else
				filteredChannels.postValue(repo.getChannelsByCountry(countryCode));
		}).start();
	}

	public LiveData<List<Channel>> getAllChannels() { return allChannels; }
	public LiveData<List<Channel>> getFilteredChannels() { return filteredChannels; }
}
