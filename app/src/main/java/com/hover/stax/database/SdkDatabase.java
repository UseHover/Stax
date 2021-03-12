package com.hover.stax.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.hover.sdk.database.DbHelper;
import com.hover.stax.actions.Action;
import com.hover.stax.actions.ActionDao;
import com.hover.stax.sims.Sim;
import com.hover.stax.sims.SimDao;

import java.io.File;

// This is a readonly database for accessing the DB created by SQL in the SDK
@Database(entities = {Action.class, Sim.class}, version = DbHelper.DATABASE_VERSION, exportSchema = false)
public abstract class SdkDatabase extends RoomDatabase {

	private static volatile SdkDatabase INSTANCE;

	public abstract ActionDao actionDao();
	public abstract SimDao simDao();

	public static synchronized SdkDatabase getInstance(Context context) {
		if (INSTANCE == null) {
			synchronized (SdkDatabase.class) {
				if (INSTANCE == null) {
			        RoomDatabase.Builder<SdkDatabase> builder = Room.databaseBuilder(context.getApplicationContext(), SdkDatabase.class, DbHelper.DATABASE_NAME);
//					RoomDatabase.Builder<SdkDatabase> builder = Room.inMemoryDatabaseBuilder(context.getApplicationContext(), SdkDatabase.class);
//					File existingDb = new File(context.getApplicationInfo().dataDir + "/databases/hoversdktransactions.db");
//					if (existingDb.exists())
//						builder.createFromFile(existingDb);

					INSTANCE = builder
//						.setJournalMode(JournalMode.TRUNCATE)
						.addMigrations(M40_41)
						.addMigrations(M41_42)
						.addMigrations(M42_43)
						.addMigrations(M43_44)
						.addMigrations(M44_45)
						.addMigrations(M45_46)
						.addMigrations(M46_47)
						.addMigrations(M47_48)
						.addMigrations(M48_49)
						.addMigrations(M49_50)
						.addMigrations(M50_51)
						.addMigrations(M51_52)
						.addMigrations(M52_53)
//						.addMigrations(M53_54)
//						.fallbackToDestructiveMigration()
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

	static final Migration M47_48 = new Migration(47, 48) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};

	static final Migration M48_49 = new Migration(48, 49) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};

	static final Migration M49_50 = new Migration(49, 50) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};

	static final Migration M50_51 = new Migration(50, 51) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};

	static final Migration M51_52 = new Migration(51, 52) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};

	static final Migration M52_53 = new Migration(52, 53) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
//			database.execSQL("ALTER TABLE hsdk_actions ADD COLUMN from_institution_logo TEXT;");
		}
	};

	static final Migration M53_54 = new Migration(53, 54) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
		}
	};
}
