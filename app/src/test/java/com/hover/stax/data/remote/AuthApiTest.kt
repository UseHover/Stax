package com.hover.stax.data.remote

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.appmattus.kotlinfixture.kotlinFixture
import com.hover.stax.data.remote.dto.authorization.NAuthRequest
import com.hover.stax.ktor.KtorClientFactory
import com.hover.stax.preferences.DefaultTokenProvider
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthApiTest {

    private val fixture = kotlinFixture()
    private lateinit var testDataStore: DataStore<Preferences>

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        testDataStore = PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("test") }
        )
    }

    @Test
    fun `test ServerError is thrown when a server exception occurs`() {
        val authRequest: NAuthRequest = fixture()
        val mockEngine = MockEngine {
            delay(500)
            respondError(HttpStatusCode.InternalServerError)
        }

        val api = StaxApi(KtorClientFactory(DefaultTokenProvider(testDataStore)).create(mockEngine))

        val actual = runBlocking { api.authorize(authRequest) }

        assert(actual is DataResult.Error)
    }

    @Test
    fun `test successful authorization`() {
        val authRequest: NAuthRequest = fixture()
        val mockEngine = MockEngine {
            respond(
                content = """{
    "redirect_url" {
        "code" : "76233958-77a5-43fc-9b3f-ca2d0d0ce54f",
        "action" : "896fa22f-d273-475b-80ee-e7553d9f9a15"
    },
    "status" : "152b1eff-f55a-4b57-9f45-0eaf5314d3c5"
}""",
                status = HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val api = StaxApi(KtorClientFactory(DefaultTokenProvider(testDataStore)).create(mockEngine))

        val actual = runBlocking { api.authorize(authRequest) }

        assert(actual is DataResult.Success)
    }
}