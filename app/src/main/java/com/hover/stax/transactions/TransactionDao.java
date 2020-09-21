package com.hover.stax.transactions;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TransactionDao {
	@Query("SELECT * FROM stax_transactions")
	LiveData<List<StaxTransaction>> getAll();

	@Query("SELECT * FROM stax_transactions WHERE transaction_type != 'balance'")
	LiveData<List<StaxTransaction>> getNonBalance();

	@Query("SELECT * FROM stax_transactions WHERE channel_id = :channelId AND transaction_type != 'balance'")
	LiveData<List<StaxTransaction>> getChannelTransactions(int channelId);

	@Query("SELECT * FROM stax_transactions WHERE transaction_type != 'balance' AND status = 'succeeded'")
	LiveData<List<StaxTransaction>> getSucceededNonBalance();

	@Query("SELECT * FROM stax_transactions WHERE uuid = :uuid LIMIT 1")
	StaxTransaction getTransaction(String uuid);

	@Query("SELECT * FROM stax_transactions WHERE uuid = :uuid LIMIT 1")
	LiveData<StaxTransaction> getLiveTransaction(String uuid);

	@Query("SELECT SUM(amount) as total FROM stax_transactions WHERE month=:month AND year =:year AND channel_id =:channelId")
	LiveData<Double> getTotalAmount(int channelId, int month, int year);


	@Insert
	void insert(StaxTransaction transaction);

	@Update
	void update(StaxTransaction transaction);
}
