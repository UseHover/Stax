package com.hover.stax.actions;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ActionDao {
	@Query("SELECT * FROM hsdk_actions WHERE is_ready = 1")
	List<Action> getAll();

	@Query("SELECT * FROM hsdk_actions WHERE server_id = :public_id LIMIT 1")
	Action getAction(String public_id);

	@Query("SELECT * FROM hsdk_actions WHERE server_id = :public_id LIMIT 1")
	LiveData<Action> getLiveAction(String public_id);

	@Query("SELECT * FROM hsdk_actions WHERE channel_id = :channel_id AND transaction_type = :transaction_type AND is_ready = 1")
	LiveData<List<Action>> getLiveActions(int channel_id, String transaction_type);

	@Query("SELECT * FROM hsdk_actions WHERE channel_id = :channel_id AND (transaction_type = 'p2p' OR transaction_type = 'me2me') AND is_ready = 1")
	List<Action> getTransferActions(int channel_id);

	@Query("SELECT * FROM hsdk_actions WHERE channel_id = :channel_id AND transaction_type = :transaction_type AND is_ready = 1")
	List<Action> getActions(int channel_id, String transaction_type);

	@Query("SELECT * FROM hsdk_actions WHERE channel_id IN (:channel_ids) AND transaction_type = :transaction_type AND is_ready = 1")
	LiveData<List<Action>> getLiveActions(int[] channel_ids, String transaction_type);

	@Query("SELECT * FROM hsdk_actions WHERE channel_id IN (:channel_ids) AND transaction_type = :transaction_type AND is_ready = 1")
	List<Action> getActions(int[] channel_ids, String transaction_type);

	@Query("SELECT * FROM hsdk_actions WHERE channel_id IN (:channel_ids) AND to_institution_id = :to_institution_id AND transaction_type = :transaction_type AND is_ready = 1")
	List<Action> getActions(int[] channel_ids, int to_institution_id, String transaction_type);

	@Query("SELECT * FROM hsdk_actions WHERE channel_id IN (:channel_ids) AND to_institution_id = :to_institution_id AND transaction_type = :transaction_type AND is_ready = 1")
	LiveData<List<Action>> getLiveActions(int[] channel_ids, int to_institution_id, String transaction_type);

	@Query("SELECT * FROM hsdk_actions WHERE channel_id IN (:channel_ids) AND to_institution_id = NULL AND from_institution_id = :from_institution_id AND transaction_type = :transaction_type AND is_ready = 1")
	LiveData<List<Action>> getLiveActionsByInst(int[] channel_ids, int from_institution_id, String transaction_type);

	@Query("SELECT * FROM hsdk_actions WHERE bounty_is_open = 1")
	LiveData<Action> getAllLiveBountyActions();
}
