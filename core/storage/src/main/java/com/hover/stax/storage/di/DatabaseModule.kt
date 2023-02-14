package com.hover.stax.storage.di

import com.hover.stax.storage.channel.repository.ChannelRepository
import com.hover.stax.storage.channel.repository.ChannelRepositoryImpl
import com.hover.stax.storage.sim.SimInfoRepository
import com.hover.stax.storage.sim.SimInfoRepositoryImpl
import com.hover.stax.storage.user.repository.StaxUserRepositoryImpl
import com.hover.stax.storage.user.repository.StaxUserRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

object DatabaseModule {

    val repository = module {
        singleOf(::StaxUserRepositoryImpl) { bind<StaxUserRepository>() }
        singleOf(::ChannelRepositoryImpl) { bind<ChannelRepository>() }
        singleOf(::SimInfoRepositoryImpl) { bind<SimInfoRepository>() }
    }
}