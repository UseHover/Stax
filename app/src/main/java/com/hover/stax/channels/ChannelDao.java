package com.hover.stax.channels;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hover.sdk.actions.HoverAction;

import java.util.List;

@Dao
public interface ChannelDao {

	@Query("SELECT * FROM channels WHERE published = 1 ORDER BY name ASC")
	LiveData<List<Channel>> getAllInAlphaOrder();

	@Query("SELECT * FROM channels WHERE selected = :selected ORDER BY defaultAccount DESC, name ASC")
	LiveData<List<Channel>> getSelected(boolean selected);

	@Query("SELECT * FROM channels WHERE id IN (:channel_ids)")
	LiveData<List<Channel>> getChannels(int[] channel_ids);

	@Query("SELECT * FROM channels WHERE country_alpha2 =:countryCode AND id IN (:channel_ids)")
	LiveData<List<Channel>> getChannels(String countryCode,int[] channel_ids );

	@Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
	Channel getChannel(int id);

	@Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
	LiveData<Channel> getLiveChannel(int id);

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
