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
@Database(entities = {Action.class}, version = 38)
public abstract class SdkDatabase extends RoomDatabase {

	private static volatile SdkDatabase INSTANCE;

	public abstract ActionDao actionDao();

	public static SdkDatabase getInstance(Context context) {
		if (INSTANCE == null) {
			synchronized (SdkDatabase.class) {
				if (INSTANCE == null) {
					INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SdkDatabase.class, "hoversdktransactions.db")
						               .addMigrations(MIGRATION_37_38)
						               .build();
				}
			}
		}
		return INSTANCE;
	}

	static final Migration MIGRATION_37_38 = new Migration(37, 38) {
		@Override
		public void migrate(SupportSQLiteDatabase database) { }
	};
}
