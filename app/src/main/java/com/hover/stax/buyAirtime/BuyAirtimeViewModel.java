package com.hover.stax.buyAirtime;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BuyAirtimeViewModel extends AndroidViewModel {

	private LiveData<List<Action>> airtimeActions;
	private LiveData<List<Channel>> selectedChannels;
	private DatabaseRepo repo;

	public BuyAirtimeViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);

		if (selectedChannels == null) {
			selectedChannels = new MutableLiveData<>();
		}
		selectedChannels = repo.getSelected();
		airtimeActions = new MutableLiveData<>();
	}

	LiveData<List<Channel>> getSelectedChannels(){return selectedChannels;}
	LiveData<List<Action>> getAirtimeActions() {return airtimeActions;}
	void setAirtimeActions(int tappedChannelId) { airtimeActions = repo.getActions(tappedChannelId, "airtime");}
	AirtimeActionModel getAirtimeActionIds(List<Action> actionList) {
		AirtimeActionModel airtimeActionModel = new AirtimeActionModel();
		if(actionList.size() > 0) {
			for(Action action : actionList) {
				String custom_steps = action.custom_steps;
				boolean isSelf = false;
				try {
					JSONArray jsonArray = new JSONArray(custom_steps);
					for(int i=0; i<jsonArray.length(); i++) {
						JSONObject object =  jsonArray.getJSONObject(i);
						if(object.get("value").equals("recipientNumber")) {
							isSelf = true;
							break;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(isSelf) airtimeActionModel.setToSelfActionId(action.public_id);
				else airtimeActionModel.setToOthersActionId(action.public_id);
			}

		}
		return airtimeActionModel;
	}

}