package com.hover.stax.database.di

import com.hover.stax.database.repository.StaxUserRepository
import com.hover.stax.database.repository.StaxUserRepositoryImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

object DatabaseModule {

    val repository = module {
        singleOf(::StaxUserRepositoryImpl) { bind<StaxUserRepository>() }
    }
}