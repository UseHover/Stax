package com.hover.stax.sims;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SimDao {
	@Query("SELECT * FROM hsdk_sims")
	LiveData<List<Sim>> getAll();

	@Query("SELECT * FROM hsdk_sims WHERE slot_idx != -1")
	List<Sim> getPresent();

	@Query("SELECT * FROM hsdk_sims WHERE imsi LIKE :hni")
	LiveData<List<Sim>> getByHni(String hni);
}
