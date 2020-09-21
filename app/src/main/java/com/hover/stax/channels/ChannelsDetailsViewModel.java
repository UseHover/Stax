package com.hover.stax.channels;

import android.app.Application;
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
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.utils.Utils;

import java.util.List;

public class ChannelsDetailsViewModel extends AndroidViewModel {
	private LiveData<List<StaxTransaction>> staxTransactions;
	private DatabaseRepo repo;

	public ChannelsDetailsViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);

	}
	void setStaxTransactions(int channelId) {
		staxTransactions = repo.getCompleteTransferTransactionsByChannelId(channelId);
	}
	public LiveData<List<StaxTransaction>> getStaxTransactions() {
		return staxTransactions;
	}
}


