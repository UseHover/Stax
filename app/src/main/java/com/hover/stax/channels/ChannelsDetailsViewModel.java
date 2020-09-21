package com.hover.stax.channels;

import android.app.Application;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.sdk.api.Hover;
import com.hover.sdk.transactions.Transaction;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.transactions.StaxDate;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.Utils;

import java.util.Date;
import java.util.List;

public class ChannelsDetailsViewModel extends AndroidViewModel {
	private LiveData<List<StaxTransaction>> staxTransactions;
	private DatabaseRepo repo;
	private LiveData<Channel> channelMutableLiveData;
	private LiveData<Double> thisMonthSpentLiveData;
	private LiveData<Double> lastMonthSpentLiveData;

	public ChannelsDetailsViewModel(@NonNull Application application) {
		super(application);
		channelMutableLiveData = new MutableLiveData<>();
		repo = new DatabaseRepo(application);

	}
	void setStaxTransactions(int channelId) {
		staxTransactions = repo.getCompleteTransferTransactionsByChannelId(channelId);
		StaxDate staxDate = DateUtils.getStaxDate(new Date().getTime());
		thisMonthSpentLiveData = repo.getSpentAmount(channelId, staxDate.getMonth(), staxDate.getYear());
		StaxDate lastMonth = DateUtils.getPreviousMonthDate(staxDate.getMonth(), staxDate.getYear());
		lastMonthSpentLiveData = repo.getSpentAmount(channelId, lastMonth.getMonth(), lastMonth.getYear());
		channelMutableLiveData = repo.getChannelV2(channelId);
	}

	LiveData<Double> getThisMonthSpentLiveData() {return  thisMonthSpentLiveData; }
	LiveData<Double> getLastMonthSpentLiveData() {return  lastMonthSpentLiveData; }
	LiveData<List<StaxTransaction>> getStaxTransactions() { return staxTransactions; }
	LiveData<Channel> getChannel() { return channelMutableLiveData;}
}


