package com.hover.stax.scheduled;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScheduleDao {

	@Query("SELECT * FROM schedules")
	LiveData<List<Schedule>> getAll();

	@Query("SELECT * FROM schedules WHERE end_date > datetime('now', 'unixepoch') ORDER BY frequency")
	LiveData<List<Schedule>> getFuture();

	@Query("SELECT * FROM schedules WHERE id = :id")
	LiveData<Schedule> get(int id);

	@Insert
	void insert(Schedule schedule);

	@Update
	void update(Schedule schedule);
}
