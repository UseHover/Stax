package com.hover.stax.contacts;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.hover.stax.schedules.Schedule;

import java.util.List;

@Dao
public interface ContactDao {

	@Query("SELECT * FROM stax_contacts")
	LiveData<List<StaxContact>> getAll();

	@Query("SELECT * FROM stax_contacts WHERE id IN (:ids)")
	List<StaxContact> get(String[] ids);

	@Query("SELECT * FROM stax_contacts WHERE id IN (:ids)")
	LiveData<List<StaxContact>> getLive(String[] ids);

	@Query("SELECT * FROM stax_contacts WHERE id  = :id LIMIT 1")
	StaxContact get(String id);

	@Query("SELECT * FROM stax_contacts WHERE id  = :id LIMIT 1")
	LiveData<StaxContact> getLive(String id);

	@Insert
	void insert(StaxContact contact);

	@Update
	void update(StaxContact contact);
}
