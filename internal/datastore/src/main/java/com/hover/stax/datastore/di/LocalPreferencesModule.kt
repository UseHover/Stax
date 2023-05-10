package com.hover.stax.datastore.di

import com.hover.stax.datastore.DefaultSharedPreferences
import com.hover.stax.datastore.LocalPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalPreferencesModule {

    @Binds
    abstract fun provideLocalPreferences(defaultSharedPreferences: DefaultSharedPreferences): LocalPreferences
}