package com.hover.stax.network.di

import android.content.Context
import com.hover.stax.datastore.TokenProvider
import com.hover.stax.network.ktor.EnvironmentProvider
import com.hover.stax.network.ktor.KtorClientFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClientEngine(): HttpClientEngine = Android.create {
        connectTimeout = 10_000
    }

    @Provides
    @Singleton
    fun providesEnvironmentProvider(
        @ApplicationContext context: Context,
    ): EnvironmentProvider = EnvironmentProvider(
        context,
    )

    @Provides
    @Singleton
    fun provideHttpClient(
        engine: HttpClientEngine,
        tokenProvider: TokenProvider,
        environmentProvider: EnvironmentProvider
    ): HttpClient =
        KtorClientFactory(tokenProvider, environmentProvider).create(engine)
}