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
@Database(entities = {Action.class, Sim.class}, version = 47, exportSchema = false)
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
						.addMigrations(M40_41)
						.addMigrations(M41_42)
						.addMigrations(M42_43)
						.addMigrations(M43_44)
						.addMigrations(M44_45)
						.addMigrations(M45_46)
						.addMigrations(M46_47)
						.build();
				}
			}
		}
		return INSTANCE;
	}

	static final Migration M40_41 = new Migration(40, 41) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};

	static final Migration M41_42 = new Migration(41, 42) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};

	static final Migration M42_43 = new Migration(42, 43) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};

	static final Migration M43_44 = new Migration(43, 44) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};

	static final Migration M44_45 = new Migration(44, 45) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};

	static final Migration M45_46 = new Migration(45, 46) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};

	static final Migration M46_47 = new Migration(46, 47) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};
}
