package com.hover.stax.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {Channel.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
	private static final int NUMBER_OF_THREADS = 4;
	static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

	private static volatile AppDatabase INSTANCE;
	public abstract ChannelDao channelDao();

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
