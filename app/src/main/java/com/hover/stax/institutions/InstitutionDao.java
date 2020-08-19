package com.hover.stax.institutions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InstitutionDao {
	@Query("SELECT * FROM institutions")
	LiveData<List<Institution>> getAll();

	@Query("SELECT * FROM institutions WHERE country_alpha2 = :countryAlpha2")
	LiveData<List<Institution>> getByCountry(String countryAlpha2);

//	@Query("SELECT * FROM institutions WHERE hni_list IN :hniList")
//	MutableLiveData<List<Institution>> getByHniList(String[] hniList);

	@Query("SELECT * FROM institutions WHERE id = :id LIMIT 1")
	Institution getInstitution(String id);

	@Insert
	void insertAll(Institution... institutions);

	@Insert
	void insert(Institution institution);

	@Update
	void update(Institution institution);

	@Delete
	void delete(Institution institution);

	@Query("DELETE FROM institutions")
	void deleteAll();
}
