package com.hover.stax.home.detailsPages.transaction;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.home.StaxTransaction;
import com.hover.stax.utils.UIHelper;

public class TransactionDetailsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transaction_details_layout);
		StaxTransaction staxTransaction = (StaxTransaction) getIntent().getParcelableExtra("staxTransaction");

		RecyclerView messagesRecyclerView = findViewById(R.id.transac_messages_recyclerView);
		messagesRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(this));
		messagesRecyclerView.setHasFixedSize(true);

		TextView amountText = findViewById(R.id.details_amount);
		TextView dateText = findViewById(R.id.details_date);
		TextView transactionNumberText = findViewById(R.id.details_transactionNumber);


		TransactionDetailsViewModel transactionDetailsViewModel = new ViewModelProvider(this).get(TransactionDetailsViewModel.class);
		if(staxTransaction !=null && staxTransaction.getUuid() !=null) {
			amountText.setText(staxTransaction.getAmount());
			dateText.setText(staxTransaction.getDateString());
			transactionNumberText.setText(staxTransaction.getUuid());


			transactionDetailsViewModel.getMessagesModels(staxTransaction.getUuid());
			transactionDetailsViewModel.loadMessagesModelObs().observe(TransactionDetailsActivity.this, model->{
				if(model !=null) messagesRecyclerView.setAdapter(new TransactionMessagesRecyclerAdapter(model));
			});
		}

	}
}
