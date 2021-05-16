package com.hover.stax.di

import com.hover.sdk.database.HoverRoomDatabase
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.database.AppDatabase
import com.hover.stax.database.DatabaseRepo
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module(true) {
    single { AppDatabase.getInstance(get()) }
    single { HoverRoomDatabase.getInstance(get()) }

    viewModel { ActionSelectViewModel(get()) }

}

val dataModule = module {
    single { DatabaseRepo(get()) }
}