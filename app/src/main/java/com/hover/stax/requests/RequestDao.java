package com.hover.stax.requests;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RequestDao {
	@Query("SELECT * FROM requests ORDER BY date_sent")
	LiveData<List<Request>> getAll();

	@Query("SELECT * FROM requests WHERE matched_transaction_uuid IS NULL ORDER BY date_sent")
	LiveData<List<Request>> getUnmatched();

	@Query("SELECT * FROM requests WHERE id = :id")
	Request get(int id);

	@Insert
	void insert(Request request);

	@Update
	void update(Request request);

	@Delete
	void delete(Request request);
}
