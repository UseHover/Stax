package com.hover.stax.ktor

import android.content.Context
import android.content.pm.ApplicationInfo
import com.hover.sdk.requests.HoverRequestContract.ENVIRONMENT
import com.hover.stax.preferences.LocalPreferences
import com.jakewharton.processphoenix.ProcessPhoenix
import timber.log.Timber

private const val PROD_BASE_URL = "https://usehover.com/stax_api/"
private const val PROD_CLIENT_ID = "bWh7AmyCO3TxKA2ohObk2mLLvKODBrv3BPMMXQ0yHhk"
private const val PROD_CLIENT_SECRET = "vQNaIvC92dXWA-M7q829BHL-89bUbKgcOuFjjXq4mZ4"
private const val PROD_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob"

private const val STAGING_BASE_URL = "https://stage.usehover.com/stax_api/"
private const val STAGING_CLIENT_ID = "O7bvQSxNE-IJhxHLUK3mk3TuHOmrioLsHZNtbQB4hLI"
private const val STAGING_CLIENT_SECRET = "20htHa1bQ-k0Vdb3H6yKaPYFD2Lt9EJpGq0qHoH4m14"
private const val STAGING_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob"

class EnvironmentProvider(
    private val context: Context,
    private val preferences: LocalPreferences
) {

    init {
        val strValue = preferences.getString(ENVIRONMENT)
        if (strValue.isNullOrEmpty()) {
            val default = defaultEnvironment()
            Timber.i("No environment set, setting default to $default")
            preferences.putString(ENVIRONMENT, default.toString())
        }
    }

    /**
     * Gets the environment that should be used for API calls
     */
    fun get(): Environment = Environment.fromString(preferences.getString(ENVIRONMENT) ?: "")

    /**
     * Sets the environment for the app to use with API calls, and restarts it. Does nothing
     * if the app is already using the requested environment.
     *
     * Can be done in a debug screen if we build one
     */
    fun setAndRestart(environment: Environment) {
        if (environment == get()) return
        preferences.putString(ENVIRONMENT, environment.toString())
        ProcessPhoenix.triggerRebirth(context)
    }

    /**
     * Returns true if we are in a debug build, and false otherwise
     */
    private fun shouldAllowEnvironmentChange(): Boolean = context.isDebuggable()

    private fun defaultEnvironment() = if (shouldAllowEnvironmentChange()) {
        Environment.STAGING
    } else {
        Environment.PROD
    }
}

enum class Environment(
    val baseUrl: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String
) {
    PROD(
        PROD_BASE_URL,
        PROD_CLIENT_ID,
        PROD_CLIENT_SECRET,
        PROD_REDIRECT_URI
    ),
    STAGING(
        STAGING_BASE_URL,
        STAGING_CLIENT_ID,
        STAGING_CLIENT_SECRET,
        STAGING_REDIRECT_URI
    );

    companion object {
        fun fromString(value: String): Environment = try {
            valueOf(value)
        } catch (ex: Exception) {
            PROD
        }
    }
}

fun Context.isDebuggable(): Boolean =
    (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0