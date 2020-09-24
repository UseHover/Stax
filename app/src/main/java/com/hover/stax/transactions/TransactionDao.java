package com.hover.stax.transactions;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TransactionDao {
	@Query("SELECT * FROM stax_transactions")
	List<StaxTransaction> getAll();

	@Query("SELECT * FROM stax_transactions WHERE transaction_type != 'balance'")
	LiveData<List<StaxTransaction>> getTransfers();

	@Query("SELECT * FROM stax_transactions WHERE channel_id = :channelId AND transaction_type != 'balance' AND status == 'succeeded'")
	LiveData<List<StaxTransaction>> getCompleteTransfers(int channelId);

	@Query("SELECT * FROM stax_transactions WHERE transaction_type != 'balance' AND status == 'succeeded'")
	LiveData<List<StaxTransaction>> getCompleteTransfers();

	@Query("SELECT * FROM stax_transactions WHERE uuid = :uuid LIMIT 1")
	StaxTransaction getTransaction(String uuid);

	@Query("SELECT * FROM stax_transactions WHERE uuid = :uuid LIMIT 1")
	LiveData<StaxTransaction> getLiveTransaction(String uuid);

	@Query("SELECT SUM(amount) as total FROM stax_transactions WHERE strftime('%m', initiated_at/1000, 'unixepoch') = :month AND strftime('%Y', initiated_at/1000, 'unixepoch') = :year AND channel_id = :channelId")
	LiveData<Double> getTotalAmount(int channelId, String month, String year);

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insert(StaxTransaction transaction);

	@Update
	void update(StaxTransaction transaction);
}
