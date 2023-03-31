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
package com.hover.stax

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hover.stax.database.Migrations
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationsTest {

    companion object {
        private const val TEST_DB = "stax-db-test"
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(), AppDatabase::class.java
    )

    @Test
    fun migrate47To48() {
        val db = helper.createDatabase(TEST_DB, 47)
        db.close()
        helper.runMigrationsAndValidate(TEST_DB, 48, true, Migrations.M47_48)
    }

    @Test
    fun migrate48To49() {
        val db = helper.createDatabase(TEST_DB, 48)
        db.close()
        helper.runMigrationsAndValidate(TEST_DB, 49, true, Migrations.M48_49)
    }

    @Test
    fun migrate50To51() {
        val db = helper.createDatabase(TEST_DB, 50)
        db.execSQL(
            "INSERT INTO accounts (id, name, alias, logo_url, account_no, institutionId, " +
                "institution_type, countryAlpha2, channelId, primary_color_hex, " +
                "secondary_color_hex, isDefault, sim_subscription_id, institutionAccountName, " +
                "latestBalance, latestBalanceTimestamp) " +
                "VALUES ('1','M-PESA','M-PESA','https://stage.usehover.com/test-logo','','573732'," +
                "'mmo','ke','662060','#39B54A','#FFFFFF','1','-1','','','1668429974096')"
        )
        db.close()
        val test = helper.runMigrationsAndValidate(TEST_DB, 51, true, Migrations.M50_51)
        val account = test.query("SELECT * FROM accounts")
        account.moveToFirst()
        MatcherAssert.assertThat(account, Is(notNullValue()))
    }

    @Test
    fun migrate51To52() {
        val db = helper.createDatabase(TEST_DB, 51)
        db.close()
        helper.runMigrationsAndValidate(TEST_DB, 52, true, Migrations.M51_52)
    }
}