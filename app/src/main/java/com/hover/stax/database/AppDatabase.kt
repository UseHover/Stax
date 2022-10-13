package com.hover.stax.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hover.stax.domain.model.Account
import com.hover.stax.data.local.accounts.AccountDao
import com.hover.stax.domain.model.Bonus
import com.hover.stax.data.local.bonus.BonusDao
import com.hover.stax.channels.Channel
import com.hover.stax.data.local.channels.ChannelDao
import com.hover.stax.contacts.ContactDao
import com.hover.stax.contacts.StaxContact
import com.hover.stax.merchants.Merchant
import com.hover.stax.merchants.MerchantDao
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
        Channel::class, StaxTransaction::class, StaxContact::class, Request::class, Schedule::class, Account::class, Paybill::class, Merchant::class, StaxUser::class, Bonus::class
    ],
    version = 46,
    autoMigrations = [
        AutoMigration(from = 36, to = 37),
        AutoMigration(from = 37, to = 38),
        AutoMigration(from = 38, to = 39),
        AutoMigration(from = 40, to = 41),
        AutoMigration(from = 41, to = 42),
        AutoMigration(from = 43, to = 44)
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

    abstract fun merchantDao(): MerchantDao

    abstract fun userDao(): UserDao

    abstract fun bonusDao(): BonusDao

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

        /**
         * These migrations recreate the paybills, requests and stax_transactions tables since room has no way of
         * changing the type of variable e.g from a nullable to non-nullable and vice versa.
         * Additional sanitization queries are included to initialize empty columns in old tables before copying into new tables
         */
        private val M39_40 = Migration(39, 40) { database ->
            //accounts table changes
            database.execSQL("ALTER TABLE accounts ADD COLUMN institutionId INTEGER")
            database.execSQL("ALTER TABLE accounts ADD COLUMN countryAlpha2 TEXT")

            //paybill table changes
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `paybills_new` (`name` TEXT NOT NULL, `business_name` TEXT, `business_no` TEXT, `account_no` TEXT, `action_id` TEXT DEFAULT ''," +
                        " `accountId` INTEGER NOT NULL, `logo_url` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recurring_amount` INTEGER NOT NULL, " +
                        "`channelId` INTEGER NOT NULL, `logo` INTEGER NOT NULL, `isSaved` INTEGER NOT NULL DEFAULT 0, " +
                        "FOREIGN KEY(`channelId`) REFERENCES `channels`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , " +
                        "FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )"
            )

            database.execSQL(
                "INSERT INTO paybills_new (name, business_no, account_no, logo, logo_url, channelId, accountId, id, recurring_amount, isSaved)" +
                        " SELECT name, business_no, account_no, logo, logo_url, channelId, accountId, id, recurring_amount, isSaved FROM paybills"
            )

            database.execSQL("DROP TABLE paybills")
            database.execSQL("ALTER TABLE paybills_new RENAME TO paybills")

            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_paybills_business_no_account_no ON paybills(business_no, account_no)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_paybills_channelId ON paybills (channelId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_paybills_accountId ON paybills (accountId)")

            //request table changes
            database.execSQL("UPDATE requests SET requester_institution_id = 0 WHERE requester_institution_id IS NULL")

            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `requests_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `description` TEXT, `requestee_ids` TEXT NOT NULL, `amount` TEXT, " +
                        "`requester_institution_id` INTEGER NOT NULL DEFAULT 0, `requester_number` TEXT, `requester_country_alpha2` TEXT, `note` TEXT, `message` TEXT, " +
                        "`matched_transaction_uuid` TEXT, `requester_account_id` INTEGER, `date_sent` INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP)",
            )

            database.execSQL(
                "INSERT INTO requests_new (id, description, requestee_ids, amount, requester_institution_id, requester_number, note, message, matched_transaction_uuid, date_sent)" +
                        " SELECT id, description, requestee_ids, amount, requester_institution_id, requester_number, note, message, matched_transaction_uuid, date_sent FROM requests"
            )

            database.execSQL("DROP TABLE requests")
            database.execSQL("ALTER TABLE requests_new RENAME TO requests")

            //stax transaction table changes
            database.execSQL(
                "UPDATE stax_transactions SET category = 'started' WHERE category IS NULL"
            )

            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `stax_transactions_new` (`uuid` TEXT NOT NULL, `action_id` TEXT NOT NULL, `environment` INTEGER NOT NULL DEFAULT 0," +
                        " `transaction_type` TEXT NOT NULL, `channel_id` INTEGER NOT NULL, `status` TEXT NOT NULL DEFAULT 'pending', `category` TEXT NOT NULL DEFAULT 'started', " +
                        "`initiated_at` INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, `updated_at` INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        " `description` TEXT NOT NULL, `account_id` INTEGER, `recipient_id` TEXT, `amount` REAL, `fee` REAL, `confirm_code` TEXT, `balance` TEXT, `note` TEXT, `account_name` TEXT)",
            )

            database.execSQL(
                "INSERT INTO stax_transactions_new (uuid, action_id, environment, transaction_type, channel_id, status, category, initiated_at, updated_at, id, description, account_id, " +
                        "recipient_id, amount, fee, confirm_code, balance) SELECT uuid, action_id, environment, transaction_type, channel_id, status, category, initiated_at, updated_at," +
                        "id, description, account_id, recipient_id, amount, fee, confirm_code, balance FROM stax_transactions"
            )

            database.execSQL("DROP TABLE stax_transactions")
            database.execSQL("ALTER TABLE stax_transactions_new RENAME TO stax_transactions")

            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_stax_transactions_uuid` ON `stax_transactions` (`uuid`)")
        }

        private val M42_43 = Migration(42, 43) { database -> //accounts table changes
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `channels_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `country_alpha2` TEXT NOT NULL, `root_code` TEXT, " +
                        "`currency` TEXT NOT NULL, `hni_list` TEXT NOT NULL, `logo_url` TEXT NOT NULL, `institution_id` INTEGER NOT NULL, `primary_color_hex` TEXT NOT NULL, `published` INTEGER NOT NULL DEFAULT 0," +
                        "`secondary_color_hex` TEXT NOT NULL, institution_type TEXT NOT NULL DEFAULT 'bank', `selected` INTEGER NOT NULL DEFAULT 0,`defaultAccount` INTEGER NOT NULL DEFAULT 0," +
                        "`isFavorite` INTEGER NOT NULL DEFAULT 0, `pin` TEXT, `latestBalance` TEXT, `latestBalanceTimestamp` INTEGER DEFAULT CURRENT_TIMESTAMP, `account_no` TEXT)",
            )

            database.execSQL(
                "INSERT INTO channels_new (id, name, country_alpha2, root_code, currency, hni_list, logo_url, institution_id, primary_color_hex, published, secondary_color_hex, selected, defaultAccount, isFavorite, pin, latestBalance, latestBalanceTimestamp, account_no)" +
                        " SELECT id, name, country_alpha2, root_code, currency, hni_list, logo_url, institution_id, primary_color_hex, published, secondary_color_hex, selected, defaultAccount, isFavorite, pin, latestBalance, latestBalanceTimestamp, account_no FROM channels"
            )
            database.execSQL("DROP TABLE channels")
            database.execSQL("ALTER TABLE channels_new RENAME TO channels")


            database.execSQL("ALTER TABLE accounts ADD COLUMN institution_type TEXT NOT NULL DEFAULT 'bank'")
            database.execSQL("ALTER TABLE accounts ADD COLUMN sim_subscription_id INTEGER NOT NULL DEFAULT -1")
        }

        private val M44_45: Migration = Migration(44, 45) {}

        private val M45_46 = Migration(45, 46) { database -> //accounts table changes
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `channels_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `country_alpha2` TEXT NOT NULL, `root_code` TEXT, " +
                        "`currency` TEXT NOT NULL, `hni_list` TEXT NOT NULL, `logo_url` TEXT NOT NULL, `institution_id` INTEGER NOT NULL, `primary_color_hex` TEXT NOT NULL, `published` INTEGER NOT NULL DEFAULT 0," +
                        "`secondary_color_hex` TEXT NOT NULL, institution_type TEXT NOT NULL DEFAULT 'bank', `isFavorite` INTEGER NOT NULL DEFAULT 0)",
            )

            database.execSQL(
                "INSERT INTO channels_new (id, name, country_alpha2, root_code, currency, hni_list, logo_url, institution_id, primary_color_hex, published, secondary_color_hex, isFavorite)" +
                        " SELECT id, name, country_alpha2, root_code, currency, hni_list, logo_url, institution_id, primary_color_hex, published, secondary_color_hex, isFavorite FROM channels"
            )
            database.execSQL("DROP TABLE channels")
            database.execSQL("ALTER TABLE channels_new RENAME TO channels")
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "stax.db")
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .addMigrations(M23_24, M24_25, M25_26, M26_27, M27_28, M28_29, M29_30, M30_31, M31_32, M32_33, M33_34, M34_35, M35_36, M39_40, M42_43, M44_45, M45_46)
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}