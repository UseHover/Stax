package com.hover.stax.channels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ChannelDao {
	@Query("SELECT * FROM channels")
	LiveData<List<Channel>> getAll();

	@Query("SELECT * FROM channels WHERE selected = :selected ORDER BY defaultAccount DESC")
	LiveData<List<Channel>> getSelected(boolean selected);

	@Query("SELECT * FROM channels WHERE country_alpha2 = :countryAlpha2")
	LiveData<List<Channel>> getByCountry(String countryAlpha2);

//	@Query("SELECT * FROM channels WHERE hni_list IN :hniList")
//	MutableLiveData<List<Channel>> getByHniList(String[] hniList);

	@Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
	Channel getChannel(String id);

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insertAll(Channel... channels);

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insert(Channel channel);

	@Update
	void update(Channel channel);

	@Delete
	void delete(Channel channel);

	@Query("DELETE FROM channels")
	void deleteAll();
}
