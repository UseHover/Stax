package com.hover.stax.data.di

import com.hover.stax.data.channel.ChannelRepository
import com.hover.stax.data.channel.ChannelRepositoryImpl
import com.hover.stax.data.sim.SimInfoRepository
import com.hover.stax.data.user.StaxUserRepository
import com.hover.stax.data.user.StaxUserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindsChannelRepository(channelRepositoryImpl: ChannelRepositoryImpl): ChannelRepository

    @Binds
    abstract fun bindsSimRepository(simRepositoryImpl: ChannelRepositoryImpl): SimInfoRepository

    @Binds
    abstract fun bindsUserRepository(staxUserRepositoryImpl: StaxUserRepositoryImpl): StaxUserRepository
}