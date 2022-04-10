package com.hover.stax.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountDao
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelDao
import com.hover.stax.contacts.ContactDao
import com.hover.stax.contacts.StaxContact
import com.hover.stax.paybill.Paybill
import com.hover.stax.paybill.PaybillDao
import com.hover.stax.requests.Request
import com.hover.stax.requests.RequestDao
import com.hover.stax.schedules.Schedule
import com.hover.stax.schedules.ScheduleDao
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.transactions.TransactionDao
import com.hover.stax.user.StaxUser
import com.hover.stax.user.UserDao
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(
    entities = [
        Channel::class, StaxTransaction::class, StaxContact::class, Request::class, Schedule::class, Account::class, Paybill::class, StaxUser::class
    ],
    version = 38,
    autoMigrations = [
        AutoMigration(from = 36, to = 37),
        AutoMigration(from = 37, to = 38)
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun channelDao(): ChannelDao

    abstract fun transactionDao(): TransactionDao

    abstract fun contactDao(): ContactDao

    abstract fun requestDao(): RequestDao

    abstract fun scheduleDao(): ScheduleDao

    abstract fun accountDao(): AccountDao

    abstract fun paybillDao(): PaybillDao

    abstract fun userDao(): UserDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val NUMBER_OF_THREADS = 8
        val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS)

        private val M23_24: Migration = Migration(23, 24) {}

        private val M24_25: Migration = Migration(24, 25) {}

        private val M25_26: Migration = Migration(25, 26) { database ->
            database.execSQL("ALTER TABLE stax_transactions ADD COLUMN environment INTEGER DEFAULT 0 NOT NULL")
        }

        private val M26_27: Migration = Migration(26, 27) {}

        private val M27_28: Migration = Migration(27, 28) { database ->
            database.execSQL("ALTER TABLE channels ADD COLUMN root_code TEXT")
        }

        private val M28_29: Migration = Migration(28, 29) { database ->
            database.execSQL("ALTER TABLE channels ADD COLUMN published INTEGER DEFAULT 0 NOT NULL")
        }

        private val M29_30: Migration = Migration(29, 30) { database ->
            database.execSQL("DROP INDEX index_stax_contacts_lookup_key")
            database.execSQL("DROP INDEX index_stax_contacts_id_phone_number")
            database.execSQL("CREATE UNIQUE INDEX index_stax_contacts_id ON stax_contacts(id)")
            database.execSQL("CREATE UNIQUE INDEX index_stax_contacts_phone_number ON stax_contacts(phone_number)")
        }

        private val M30_31: Migration = Migration(30, 31) { database ->
            database.execSQL("ALTER TABLE stax_transactions ADD COLUMN category TEXT")
        }

        private val M31_32: Migration = Migration(31, 32) { database ->
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS accounts (name TEXT NOT NULL, alias TEXT NOT NULL, logo_url TEXT NOT NULL, " +
                        "account_no TEXT, channelId INTEGER NOT NULL, primary_color_hex TEXT NOT NULL, secondary_color_hex TEXT NOT NULL, " +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, latestBalance TEXT, latestBalanceTimestamp INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(channelId) REFERENCES channels(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
            )
            database.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_channelId ON accounts(channelId)")
        }

        private val M32_33: Migration = Migration(32, 33) { database ->
            database.execSQL("ALTER TABLE accounts ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0")
        }

        private val M33_34: Migration = Migration(33, 34) { database ->
            database.execSQL("ALTER TABLE stax_transactions ADD COLUMN account_id INTEGER")
        }

        private val M34_35: Migration = Migration(34, 35) { database ->
            database.execSQL("ALTER TABLE stax_transactions ADD COLUMN balance TEXT")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_accounts_name ON accounts(name)")
        }

        private val M35_36: Migration = Migration(35, 36) { database ->
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS paybills (name TEXT NOT NULL, business_no TEXT NOT NULL, account_no TEXT, logo INTEGER NOT NULL, " +
                        "logo_url TEXT NOT NULL, channelId INTEGER NOT NULL, accountId INTEGER NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, recurring_amount INTEGER NOT NULL," +
                        " isSaved INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(channelId) REFERENCES channels(id) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(accountId)" +
                        " REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE NO ACTION )"
            )
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_paybills_business_no_account_no ON paybills(business_no, account_no)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_paybills_channelId ON paybills (channelId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_paybills_accountId ON paybills (accountId)")
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "stax.db")
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .addMigrations(M23_24, M24_25, M25_26, M26_27, M27_28, M28_29, M29_30, M30_31, M31_32, M33_34, M34_35, M35_36)
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}