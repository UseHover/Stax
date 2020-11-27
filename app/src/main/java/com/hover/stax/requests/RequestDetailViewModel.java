package com.hover.stax.requests;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.utils.Utils;

import java.util.List;

public class RequestDetailViewModel extends AndroidViewModel {
	private final String TAG = "RequestDetailViewModel";

	private DatabaseRepo repo;
	private MutableLiveData<Request> request;
	private LiveData<List<StaxContact>> recipients = new MutableLiveData<>();

	public RequestDetailViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		request = new MutableLiveData<>();
		recipients = Transformations.switchMap(request, this::loadRecipients);
	}

	public void setRequest(int id) {
		new Thread(() -> request.postValue(repo.getRequest(id))).start();
	}

	public LiveData<Request> getRequest() {
		if (request == null) {
			return new MutableLiveData<>();
		}
		return request;
	}

	public LiveData<List<StaxContact>> loadRecipients(Request r) {
		return repo.getLiveContacts(r.requestee_ids.split(","));
	}

	public LiveData<List<StaxContact>> getRecipients() {
		if (recipients == null) { return new MutableLiveData<>(); }
		return recipients;
	}

	String generateRecipientString() {
		StringBuilder phones = new StringBuilder();
		List<StaxContact> rs = recipients.getValue();
		if(rs!=null) {
			for (int r = 0; r < rs.size(); r++) {
				if (phones.length() > 0) phones.append(",");
				phones.append(rs.get(r).phoneNumber);
			}
			return phones.toString();
		}
		else return "";


	}

	void getCountryAlphaAndSendWithWhatsApp(Context context, Activity activity) {
		Request requestValue = request.getValue();
		if(requestValue !=null) {
			new Thread(() -> {
				Channel channel = repo.getChannelByInstitutionId(requestValue.requester_institution_id);
				Request.sendUsingWhatsapp(generateRecipientString(), channel.countryAlpha2, generateSMS(), context, activity);
			}).start();
		}
	}

	String generateSMS() {
		Request requestValue = request.getValue();
		if(requestValue !=null) {
			String amountString = requestValue.amount != null ? getApplication().getString(R.string.sms_amount_detail, Utils.formatAmount(requestValue.amount)) : "";
			String noteString = requestValue.note != null ? getApplication().getString(R.string.sms_note_detail, requestValue.note) : "";

			String amountNoFormat = requestValue.amount != null ? requestValue.amount : "0.00";
			int institution_id = requestValue.requester_institution_id;
			String accountNumber= requestValue.requester_number !=null ? requestValue.requester_number.trim() : "";

			String paymentLink = Request.generateStaxLink(amountNoFormat, institution_id, accountNumber, requestValue.date_sent, getApplication());

			if (paymentLink !=null) return getApplication().getString(R.string.sms_request_template_with_link, amountString, noteString, paymentLink);
			else return getApplication().getString(R.string.sms_request_template_no_link, amountString, noteString);
		}
		else return  null;
	}

	void deleteRequest() {
		repo.delete(request.getValue());
	}
}
