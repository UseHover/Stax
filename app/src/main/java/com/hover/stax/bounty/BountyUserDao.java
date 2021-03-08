package com.hover.stax.bounty;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface BountyUserDao {
	@Query("SELECT * FROM bountyUser WHERE isUploaded = 0 ORDER BY timestamp ASC LIMIT 1")
	BountyUser getUser();

	@Query("SELECT COUNT(deviceId) FROM bountyUser")
	LiveData<Integer> getEntriesCount();

	@Insert(onConflict = OnConflictStrategy.ABORT)
	void insert(BountyUser bountyUser);

	@Update(onConflict = OnConflictStrategy.IGNORE)
	void update(BountyUser bountyUser);
}
