package com.hover.stax.schedules;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.DatabaseRepo;

import java.util.List;

import static org.koin.java.KoinJavaComponent.get;

public class ScheduleDetailViewModel extends AndroidViewModel {

    private DatabaseRepo repo = get(DatabaseRepo.class);
    private MutableLiveData<Schedule> schedule;
    private LiveData<HoverAction> action;
    private LiveData<List<StaxContact>> contacts;

    public ScheduleDetailViewModel(@NonNull Application application) {
        super(application);
        schedule = new MutableLiveData<>();
        action = Transformations.switchMap(schedule, this::loadAction);
        contacts = Transformations.switchMap(schedule, this::loadContacts);
    }

    public void setSchedule(int id) {
        new Thread(() -> schedule.postValue(repo.getSchedule(id))).start();
    }

    public LiveData<Schedule> getSchedule() {
        if (schedule == null) {
            return new MutableLiveData<>();
        }
        return schedule;
    }

    private LiveData<HoverAction> loadAction(Schedule s) {
        if (s != null) {
            return repo.getLiveAction(s.action_id);
        }
        return new MutableLiveData<>();
    }

    public LiveData<HoverAction> getAction() {
        if (action == null) {
            action = new MutableLiveData<>();
        }
        return action;
    }

    private LiveData<List<StaxContact>> loadContacts(Schedule s) {
        if (s != null) {
            return repo.getLiveContacts(s.recipient_ids.split(","));
        }
        return null;
    }

    public LiveData<List<StaxContact>> getContacts() {
        if (contacts == null) {
            contacts = new MutableLiveData<>();
        }
        return contacts;
    }

    void deleteSchedule() {
        repo.delete(schedule.getValue());
    }
}
