package com.hover.stax.buyAirtime;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BuyAirtimeViewModel extends AndroidViewModel {

	private LiveData<List<Channel>> selectedChannels;
	private DatabaseRepo repo;

	public BuyAirtimeViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);

		if (selectedChannels == null) {
			selectedChannels = new MutableLiveData<>();
		}
		selectedChannels = repo.getSelected();

	}

	LiveData<List<Channel>> getSelectedChannels(){return selectedChannels;}
	LiveData<List<Action>> getAirtimeActions(int tappedChannelId) { return repo.getActions(tappedChannelId, "airtime");}

	AirtimeActionModel getAirtimeActionModel(List<Action> actionList) {
		AirtimeActionModel airtimeActionModel = new AirtimeActionModel();
		if(actionList.size() > 0) {
			for(Action action : actionList) {
				String custom_steps = action.custom_steps;
				boolean isSelf = true;
				try {
					JSONArray jsonArray = new JSONArray(custom_steps);
					for(int i=0; i<jsonArray.length(); i++) {
						JSONObject object =  jsonArray.getJSONObject(i);
						if(object.get("value").equals("recipientNumber")) {
							isSelf = false;
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