package com.hover.stax.transfers;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amplitude.api.Amplitude;
import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.schedules.Schedule;

import java.util.List;

public abstract class AbstractFormViewModel extends AndroidViewModel {

    protected DatabaseRepo repo;
    protected String type = HoverAction.P2P;

    protected LiveData<List<StaxContact>> recentContacts = new MutableLiveData<>();
    protected MutableLiveData<Schedule> schedule = new MutableLiveData<>();
    protected MutableLiveData<Boolean> isEditing = new MutableLiveData<>();

    public AbstractFormViewModel(@NonNull Application application) {
        super(application);
        repo = new DatabaseRepo(application);

        isEditing.setValue(true);
        recentContacts = repo.getAllContacts();
    }

    public String getType() {
        return type;
    }

    public void setEditing(boolean isEdit) {
        isEditing.setValue(isEdit);
    }

    public LiveData<Boolean> getIsEditing() {
        if (isEditing == null) {
            isEditing = new MutableLiveData<>();
            isEditing.setValue(false);
        }
        return isEditing;
    }

    public LiveData<List<StaxContact>> getRecentContacts() {
        if (recentContacts == null) {
            recentContacts = new MutableLiveData<>();
        }
        return recentContacts;
    }

    protected void saveSchedule(Schedule s) {
        Amplitude.getInstance().logEvent(getApplication().getString(R.string.scheduled_complete, s.type));
        repo.insert(s);
    }
}
