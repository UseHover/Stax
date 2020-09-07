package com.hover.stax.actions;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ActionDao {
	@Query("SELECT * FROM hsdk_actions")
	LiveData<List<Action>> getAll();

	@Query("SELECT * FROM hsdk_actions WHERE server_id = :public_id LIMIT 1")
	Action getAction(String public_id);

	@Query("SELECT * FROM hsdk_actions WHERE from_institution_id = :institution_id")
	LiveData<List<Action>> getByFromInstitution(int institution_id);

	@Query("SELECT * FROM hsdk_actions WHERE to_institution_id = :institution_id")
	LiveData<List<Action>> getByToInstitution(int institution_id);

	@Query("SELECT * FROM hsdk_actions WHERE channel_id = :channel_id AND transaction_type = :transaction_type")
	LiveData<List<Action>> getActions(int channel_id, String transaction_type);

	@Query("SELECT * FROM hsdk_actions WHERE channel_id IN (:channel_ids) AND transaction_type = :transaction_type")
	LiveData<List<Action>> getActions(int[] channel_ids, String transaction_type);
}
