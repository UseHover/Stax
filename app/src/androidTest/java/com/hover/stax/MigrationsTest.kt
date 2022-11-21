package com.hover.stax

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hover.stax.database.AppDatabase
import com.hover.stax.database.Migrations
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class MigrationsTest {

    companion object {
        private const val TEST_DB = "stax-db-test"
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
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
            "INSERT INTO accounts (id, name, alias, logo_url, account_no, institutionId, institution_type, countryAlpha2, " +
                    "channelId, primary_color_hex, secondary_color_hex, isDefault, sim_subscription_id, institutionAccountName, " +
                    "latestBalance, latestBalanceTimestamp) VALUES ('1','M-PESA','M-PESA','https://stage.usehover.com//rails/active_storage/blobs/eyJfcmFpbHMiOnsibWVzc2FnZSI6IkJBaHBBZU09IiwiZXhwIjpudWxsLCJwdXIiOiJibG9iX2lkIn19--d9824d3caa5d9799109fab40f1b088968a13b2d6/test-logo','','573732','mmo','ke','662060','#39B54A','#FFFFFF','1','-1','','','1668429974096')"
        )
        db.close()
        val test = helper.runMigrationsAndValidate(TEST_DB, 51, true, Migrations.M50_51)
        val account = test.query("SELECT * FROM accounts")
        account.moveToFirst()
        MatcherAssert.assertThat(account, Is(notNullValue()))
    }

}