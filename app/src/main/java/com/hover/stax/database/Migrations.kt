/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.database

import androidx.room.migration.Migration

class Migrations {

    companion object {

        val M23_24: Migration = Migration(23, 24) {}

        val M24_25: Migration = Migration(24, 25) {}

        val M25_26: Migration = Migration(25, 26) { database ->
            database.execSQL("ALTER TABLE stax_transactions ADD COLUMN environment INTEGER DEFAULT 0 NOT NULL")
        }

        val M26_27: Migration = Migration(26, 27) {}

        val M27_28: Migration = Migration(27, 28) { database ->
            database.execSQL("ALTER TABLE channels ADD COLUMN root_code TEXT")
        }

        val M28_29: Migration = Migration(28, 29) { database ->
            database.execSQL("ALTER TABLE channels ADD COLUMN published INTEGER DEFAULT 0 NOT NULL")
        }

        val M29_30: Migration = Migration(29, 30) { database ->
            database.execSQL("DROP INDEX index_stax_contacts_lookup_key")
            database.execSQL("DROP INDEX index_stax_contacts_id_phone_number")
            database.execSQL("CREATE UNIQUE INDEX index_stax_contacts_id ON stax_contacts(id)")
            database.execSQL("CREATE UNIQUE INDEX index_stax_contacts_phone_number ON stax_contacts(phone_number)")
        }

        val M30_31: Migration = Migration(30, 31) { database ->
            database.execSQL("ALTER TABLE stax_transactions ADD COLUMN category TEXT")
        }

        val M31_32: Migration = Migration(31, 32) { database ->
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS accounts (name TEXT NOT NULL, alias TEXT NOT NULL, logo_url TEXT NOT NULL, " +
                    "account_no TEXT, channelId INTEGER NOT NULL, primary_color_hex TEXT NOT NULL, secondary_color_hex TEXT NOT NULL, " +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, latestBalance TEXT, latestBalanceTimestamp INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(channelId) REFERENCES channels(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
            )
            database.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_channelId ON accounts(channelId)")
        }

        val M32_33: Migration = Migration(32, 33) { database ->
            database.execSQL("ALTER TABLE accounts ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0")
        }

        val M33_34: Migration = Migration(33, 34) { database ->
            database.execSQL("ALTER TABLE stax_transactions ADD COLUMN account_id INTEGER")
        }

        val M34_35: Migration = Migration(34, 35) { database ->
            database.execSQL("ALTER TABLE stax_transactions ADD COLUMN balance TEXT")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_accounts_name ON accounts(name)")
        }

        val M35_36: Migration = Migration(35, 36) { database ->
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
        val M39_40 = Migration(39, 40) { database ->
            // accounts table changes
            database.execSQL("ALTER TABLE accounts ADD COLUMN institutionId INTEGER")
            database.execSQL("ALTER TABLE accounts ADD COLUMN countryAlpha2 TEXT")

            // paybill table changes
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

            // request table changes
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

            // stax transaction table changes
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

        val M42_43 = Migration(42, 43) { database -> // accounts table changes
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

        val M44_45: Migration = Migration(44, 45) {}

        val M45_46 = Migration(45, 46) { database ->
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

        val M47_48 = Migration(47, 48) { database ->
            database.execSQL("DROP INDEX IF EXISTS index_accounts_name")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_accounts_name_sim_subscription_id ON accounts(name, sim_subscription_id)")
        }

        val M48_49 = Migration(48, 49) { database ->
            database.execSQL("DROP TABLE bonuses")
        }

        val M50_51 = Migration(50, 51) { database ->
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `accounts_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `alias` TEXT NOT NULL, `logo_url` TEXT NOT NULL, " +
                    "`account_no` TEXT, `institutionId` INTEGER DEFAULT -1, `institution_type` TEXT DEFAULT 'bank', `countryAlpha2` TEXT, `channelId` INTEGER," +
                    "`primary_color_hex` TEXT, `secondary_color_hex` TEXT, `isDefault` INTEGER NOT NULL DEFAULT 0, `sim_subscription_id` INTEGER NOT NULL DEFAULT -1, `institutionAccountName` TEXT," +
                    "`latestBalance` TEXT, `latestBalanceTimestamp` INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(channelId) REFERENCES channels(id) ON UPDATE NO ACTION ON DELETE NO ACTION)",
            )

            database.execSQL(
                "INSERT INTO accounts_new (id, name, alias, logo_url, account_no, institutionId, institution_type, countryAlpha2, channelId, primary_color_hex, secondary_color_hex, isDefault, sim_subscription_id, institutionAccountName, latestBalance, latestBalanceTimestamp)" +
                        " SELECT a.id, a.name, a.alias, a.logo_url, a.account_no, c.institution_id, c.institution_type, LOWER(c.country_alpha2), c.id, c.primary_color_hex, c.secondary_color_hex, a.isDefault, a.sim_subscription_id, a.institutionAccountName, a.latestBalance, a.latestBalanceTimestamp" +
                        " FROM accounts as a LEFT JOIN channels AS c ON a.channelId = c.id"
            )
            database.execSQL("DROP TABLE accounts")
            database.execSQL("ALTER TABLE accounts_new RENAME TO accounts")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_accounts_name_sim_subscription_id` ON `accounts` (`name`, `sim_subscription_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_accounts_channelId` ON `accounts` (`channelId`)")
        }

        val M51_52 = Migration(51, 52) { database ->
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `accounts_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `alias` TEXT NOT NULL, `logo_url` TEXT NOT NULL, " +
                        "`account_no` TEXT, `institutionId` INTEGER NOT NULL, `institution_type` TEXT NOT NULL DEFAULT 'bank', `countryAlpha2` TEXT NOT NULL, `channelId` INTEGER NOT NULL," +
                        "`primary_color_hex` TEXT NOT NULL, `secondary_color_hex` TEXT NOT NULL, `isDefault` INTEGER NOT NULL DEFAULT 0, `sim_subscription_id` INTEGER NOT NULL DEFAULT -1, `institutionAccountName` TEXT," +
                        "`latestBalance` TEXT, `latestBalanceTimestamp` INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(channelId) REFERENCES channels(id) ON UPDATE NO ACTION ON DELETE NO ACTION)",
            )

            database.execSQL(
                "INSERT INTO accounts_new (id, name, alias, logo_url, account_no, institutionId, institution_type, countryAlpha2, channelId, primary_color_hex, secondary_color_hex, isDefault, sim_subscription_id, institutionAccountName, latestBalance, latestBalanceTimestamp)" +
                        " SELECT a.id, a.name, a.alias, a.logo_url, a.account_no, c.institution_id, c.institution_type, LOWER(c.country_alpha2), c.id, c.primary_color_hex, c.secondary_color_hex, a.isDefault, a.sim_subscription_id, a.institutionAccountName, a.latestBalance, a.latestBalanceTimestamp" +
                        " FROM accounts as a LEFT JOIN channels AS c ON a.channelId = c.id"
            )
            database.execSQL("DROP TABLE accounts")
            database.execSQL("ALTER TABLE accounts_new RENAME TO accounts")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_accounts_name_sim_subscription_id` ON `accounts` (`name`, `sim_subscription_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_accounts_channelId` ON `accounts` (`channelId`)")

            database.execSQL("DROP TABLE stax_users")
            database.execSQL("CREATE TABLE IF NOT EXISTS `stax_users` (`id` INTEGER PRIMARY KEY NOT NULL, `username` TEXT NOT NULL, `email` TEXT NOT NULL, `isMapper` INTEGER NOT NULL DEFAULT 0, `marketingOptedIn` INTEGER NOT NULL DEFAULT 0, `transactionCount` INTEGER NOT NULL, `bountyTotal` INTEGER NOT NULL, `totalPoints` INTEGER NOT NULL DEFAULT 0)")
        }
    }
}