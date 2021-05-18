package com.hover.stax.schedules;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScheduleDao {

    @Query("SELECT * FROM schedules")
    LiveData<List<Schedule>> getAll();

    // Epoch ms minus one day
    @Query("SELECT * FROM schedules WHERE start_date > (strftime('%s','now')*1000 - 86400000) AND complete = 0 ORDER BY frequency, start_date DESC")
    LiveData<List<Schedule>> getLiveFuture();

    @Query("SELECT * FROM schedules WHERE start_date > (strftime('%s','now')*1000 - 86400000) AND complete = 0 ORDER BY frequency, start_date DESC")
    List<Schedule> getFuture();

    @Query("SELECT * FROM schedules WHERE id = :id")
    Schedule get(int id);

    @Insert
    void insert(Schedule schedule);

    @Update
    void update(Schedule schedule);

    @Delete
    void delete(Schedule schedule);
}
