package com.hover.stax.actions;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.hover.stax.R;
import com.hover.stax.utils.fieldstates.FieldState;
import com.hover.stax.utils.fieldstates.FieldStateType;
import com.hover.stax.utils.fieldstates.Validation;

import java.util.List;

public class ActionSelectViewModel extends AndroidViewModel {
	final private String TAG = "ActionSelectViewModel";

	private MediatorLiveData<List<Action>> filteredActions = new MediatorLiveData<>();
	private MediatorLiveData<Action> activeAction = new MediatorLiveData<>();

	private MediatorLiveData<FieldState> actionFieldState = new MediatorLiveData<>();

	public ActionSelectViewModel(Application application) {
		super(application);
		activeAction.addSource(filteredActions, this::setActiveActionIfOutOfDate);
		actionFieldState.addSource(activeAction, activeAction -> { if (activeAction != null) actionFieldState.setValue(null); });
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

	public LiveData<FieldState> getActiveActionFieldState() {
		if (actionFieldState == null) { actionFieldState = new MediatorLiveData<>(); }
		return actionFieldState;
	}

	public boolean validates(Validation validationType) {
		boolean valid = true;
		if (activeAction.getValue() == null) {
			if(validationType == Validation.HARD) {
				valid = false;
				actionFieldState.setValue(new FieldState(FieldStateType.ERROR, getApplication().getString(R.string.action_fielderror)));
			}
		} else actionFieldState.setValue(new FieldState(FieldStateType.SUCCESS, ""));
		Log.e(TAG, "is valid? " + valid);
		return valid;
	}

	boolean requiresActionChoice() { // in last case, should have request network as choice
		return filteredActions.getValue() != null && filteredActions.getValue().size() > 0 && (filteredActions.getValue().size() > 1 || filteredActions.getValue().get(0).hasDiffToInstitution());
	}

	boolean hasActionsLoaded() {
		return filteredActions.getValue() != null && filteredActions.getValue().size() > 0 &&
			       (filteredActions.getValue().size() > 1 || (activeAction.getValue() != null && activeAction.getValue().hasToInstitution()));
	}
}
