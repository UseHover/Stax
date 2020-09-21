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
@Database(entities = {Action.class, Sim.class}, version = 41)
public abstract class SdkDatabase extends RoomDatabase {

	private static volatile SdkDatabase INSTANCE;

	public abstract ActionDao actionDao();
	public abstract SimDao simDao();

	public static SdkDatabase getInstance(Context context) {
		if (INSTANCE == null) {
			synchronized (SdkDatabase.class) {
				if (INSTANCE == null) {
					INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SdkDatabase.class, "hoversdktransactions.db")
									   .addMigrations(MIGRATION_40_41)
									   .build();
				}
			}
		}
		return INSTANCE;
	}

	static final Migration MIGRATION_40_41 = new Migration(40, 41) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};
}
