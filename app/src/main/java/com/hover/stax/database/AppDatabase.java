package com.hover.stax.database;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Transaction;

import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDao;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.transactions.TransactionDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {Channel.class, StaxTransaction.class}, version = 8)
public abstract class AppDatabase extends RoomDatabase {
	private static final int NUMBER_OF_THREADS = 4;
	static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

	private static volatile AppDatabase INSTANCE;

	public abstract ChannelDao channelDao();
	public abstract TransactionDao transactionDao();

	public static AppDatabase getInstance(Context context) {
		if (INSTANCE == null) {
			synchronized (AppDatabase.class) {
				if (INSTANCE == null) {
					INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "stax.db")
									   .fallbackToDestructiveMigration()
									   .build();
				}
			}
		}
		return INSTANCE;
	}
}
