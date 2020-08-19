package com.hover.stax.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.hover.stax.institutions.Institution;
import com.hover.stax.institutions.InstitutionDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {Institution.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
	private static final int NUMBER_OF_THREADS = 4;
	static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

	private static volatile AppDatabase INSTANCE;
	public abstract InstitutionDao institutionDao();

	public static AppDatabase getInstance(Context context) {
		if (INSTANCE == null) {
			synchronized (AppDatabase.class) {
				if (INSTANCE == null) {
					INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "stax.db").build();
				}
			}
		}
		return INSTANCE;
	}
}
