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

	@Query("SELECT * FROM stax_contacts ORDER BY name, phone_number, last_used_timestamp")
	LiveData<List<StaxContact>> getAll();

	@Query("SELECT * FROM stax_contacts WHERE id IN (:ids) ORDER BY name, phone_number, last_used_timestamp")
	List<StaxContact> get(String[] ids);

	@Query("SELECT * FROM stax_contacts WHERE id IN (:ids)")
	LiveData<List<StaxContact>> getLive(String[] ids);

	@Query("SELECT * FROM stax_contacts WHERE id  = :id LIMIT 1")
	StaxContact get(String id);

	@Query("SELECT * FROM stax_contacts WHERE lookup_key  = :lookupKey LIMIT 1")
	StaxContact lookup(String lookupKey);

	@Query("SELECT * FROM stax_contacts WHERE id  = :id LIMIT 1")
	LiveData<StaxContact> getLive(String id);

	@Query("SELECT * FROM stax_contacts WHERE phone_number =:phone LIMIT 1")
	StaxContact getContact(String phone);

	@Insert
	void insert(StaxContact contact);

	@Update
	void update(StaxContact contact);
}
