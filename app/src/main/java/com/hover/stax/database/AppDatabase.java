package com.hover.stax.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.hover.stax.account.Account;
import com.hover.stax.account.AccountDao;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDao;
import com.hover.stax.contacts.ContactDao;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.requests.Request;
import com.hover.stax.requests.RequestDao;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.schedules.ScheduleDao;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.transactions.TransactionDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Channel.class, StaxTransaction.class, StaxContact.class, Request.class, Schedule.class, Account.class}, version = 32)
public abstract class AppDatabase extends RoomDatabase {
    private static final int NUMBER_OF_THREADS = 8;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static volatile AppDatabase INSTANCE;

    public abstract ChannelDao channelDao();

    public abstract TransactionDao transactionDao();

    public abstract ContactDao contactDao();

    public abstract RequestDao requestDao();

    public abstract ScheduleDao scheduleDao();

    public abstract AccountDao accountDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "stax.db")
                            .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                            .addMigrations(M23_24)
                            .addMigrations(M24_25)
                            .addMigrations(M25_26)
                            .addMigrations(M26_27)
                            .addMigrations(M27_28)
                            .addMigrations(M28_29)
                            .addMigrations(M29_30)
                            .addMigrations(M30_31)
                            .addMigrations(M31_32)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration M23_24 = new Migration(23, 24) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
        }
    };

    static final Migration M24_25 = new Migration(24, 25) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
        }
    };

    static final Migration M25_26 = new Migration(25, 26) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE stax_transactions ADD COLUMN environment INTEGER DEFAULT 0 NOT NULL");
        }
    };

    static final Migration M26_27 = new Migration(26, 27) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
        }
    };

    static final Migration M27_28 = new Migration(27, 28) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE channels ADD COLUMN root_code TEXT");
        }
    };

    static final Migration M28_29 = new Migration(28, 29) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE channels ADD COLUMN published INTEGER DEFAULT 0 NOT NULL");
        }
    };

    static final Migration M29_30 = new Migration(29, 30) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("DROP INDEX index_stax_contacts_lookup_key");
            database.execSQL("DROP INDEX index_stax_contacts_id_phone_number");
            database.execSQL("CREATE UNIQUE INDEX index_stax_contacts_id ON stax_contacts(id)");
            database.execSQL("CREATE UNIQUE INDEX index_stax_contacts_phone_number ON stax_contacts(phone_number)");
        }
    };

    static final Migration M30_31 = new Migration(30, 31) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE stax_transactions ADD COLUMN category TEXT");
        }
    };

    static final Migration M31_32 = new Migration(31, 32) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS accounts (name TEXT NOT NULL, alias TEXT NOT NULL, logo_url TEXT NOT NULL, " +
                    "account_no TEXT, channelId INTEGER NOT NULL, primary_color_hex TEXT NOT NULL, secondary_color_hex TEXT NOT NULL, " +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, latestBalance TEXT, latestBalanceTimestamp INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(channelId) REFERENCES channels(id) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_channelId ON accounts(channelId)");
        }
    };
}
