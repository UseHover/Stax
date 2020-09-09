package com.hover.stax.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.hover.stax.actions.Action;
import com.hover.stax.actions.ActionDao;

// This is a readonly database for accessing the DB created by SQL in the SDK
@Database(entities = {Action.class}, version = 39)
public abstract class SdkDatabase extends RoomDatabase {

	private static volatile SdkDatabase INSTANCE;

	public abstract ActionDao actionDao();

	public static SdkDatabase getInstance(Context context) {
		if (INSTANCE == null) {
			synchronized (SdkDatabase.class) {
				if (INSTANCE == null) {
					INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SdkDatabase.class, "hoversdktransactions.db")
									   .allowMainThreadQueries()
									   .addMigrations(MIGRATION_38_39)
									   .build();
				}
			}
		}
		return INSTANCE;
	}

	static final Migration MIGRATION_38_39 = new Migration(38, 39) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};
}
