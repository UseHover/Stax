/*
 * Copyright 2023 Stax
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
package com.hover.stax.network.di

import android.content.Context
import com.hover.stax.datastore.LocalPreferences
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
        localPreferences: LocalPreferences
    ): EnvironmentProvider = EnvironmentProvider(
        context,
        localPreferences
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