package com.hover.stax.requests;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RequestDao {
	@Query("SELECT * FROM requests ORDER BY date_sent")
	LiveData<List<Request>> getAll();

	@Query("SELECT * FROM requests WHERE id = :id")
	LiveData<Request> get(int id);

	@Insert
	void insert(Request request);

	@Update
	void update(Request request);
}
