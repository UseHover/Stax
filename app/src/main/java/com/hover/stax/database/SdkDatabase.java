package com.hover.stax.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.hover.stax.actions.Action;
import com.hover.stax.actions.ActionDao;
import com.hover.stax.sims.Sim;
import com.hover.stax.sims.SimDao;

// This is a readonly database for accessing the DB created by SQL in the SDK
@Database(entities = {Action.class, Sim.class}, version = 42, exportSchema = false)
public abstract class SdkDatabase extends RoomDatabase {

	private static volatile SdkDatabase INSTANCE;

	public abstract ActionDao actionDao();

	public abstract SimDao simDao();

	public static synchronized SdkDatabase getInstance(Context context) {
		if (INSTANCE == null) {
			synchronized (SdkDatabase.class) {
				if (INSTANCE == null) {
					INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SdkDatabase.class, "hoversdktransactions.db")
									   .setJournalMode(JournalMode.TRUNCATE)
									   .addMigrations(MIGRATION_41_42)
									   .build();
				}
			}
		}
		return INSTANCE;
	}

	static final Migration MIGRATION_41_42 = new Migration(41, 42) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};
}
