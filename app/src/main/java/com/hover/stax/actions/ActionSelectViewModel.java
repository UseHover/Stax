package com.hover.stax.actions;

import android.app.Application;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.hover.stax.R;

import java.util.List;

public class ActionSelectViewModel extends AndroidViewModel {
	final private String TAG = "ActionSelectViewModel";

	private MediatorLiveData<List<Action>> filteredActions = new MediatorLiveData<>();
	private MediatorLiveData<Action> activeAction = new MediatorLiveData<>();

	public ActionSelectViewModel(Application application) {
		super(application);
		activeAction.addSource(filteredActions, this::setActiveActionIfOutOfDate);
	}

	public void setActions(List<Action> actions) {
		filteredActions.postValue(actions);
	}

	public LiveData<List<Action>> getActions() {
		return filteredActions;
	}

	private void setActiveActionIfOutOfDate(List<Action> actions) {
		Log.e(TAG, "maybe setting active action");
		if (actions != null && actions.size() > 0 && (activeAction.getValue() == null || !actions.contains(activeAction.getValue()))) {
			Log.e(TAG, "Auto selecting: " + actions.get(0) + " " + actions.get(0).transaction_type + " " + actions.get(0).recipientInstitutionId() + " " + actions.get(0).public_id);
			activeAction.setValue(actions.get(0));
		}
	}

	public void setActiveAction(Action action) {
		activeAction.postValue(action);
	}

	public LiveData<Action> getActiveAction() {
		if (activeAction == null) { activeAction = new MediatorLiveData<>(); }
		return activeAction;
	}

	public String errorCheck() {
		return activeAction.getValue() == null ? getApplication().getString(R.string.action_fielderror) : null;
	}

	boolean requiresActionChoice() { // in last case, should have request network as choice
		return filteredActions.getValue() != null && filteredActions.getValue().size() > 0 && (filteredActions.getValue().size() > 1 || filteredActions.getValue().get(0).hasDiffToInstitution());
	}

	boolean hasActionsLoaded() {
		return filteredActions.getValue() != null && filteredActions.getValue().size() > 0 &&
			       (filteredActions.getValue().size() > 1 || (activeAction.getValue() != null && activeAction.getValue().hasToInstitution()));
	}
}
