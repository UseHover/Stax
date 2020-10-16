package com.hover.stax.contacts;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContactDao {

	@Query("SELECT * FROM stax_contacts")
	LiveData<List<StaxContact>> getAll();

	@Insert
	void insert(StaxContact contact);

	@Update
	void update(StaxContact contact);
}
