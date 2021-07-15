package com.hover.stax.di

import com.hover.sdk.database.HoverRoomDatabase
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.balances.BalancesViewModel
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.database.AppDatabase
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.faq.FaqViewModel
import com.hover.stax.requests.NewRequestViewModel
import com.hover.stax.schedules.ScheduleDetailViewModel
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.transfers.TransferViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module(true) {
    single { AppDatabase.getInstance(get()) }
    single { HoverRoomDatabase.getInstance(get()) }

    viewModel { FaqViewModel(get()) }
    viewModel { ActionSelectViewModel(get()) }
    viewModel { ChannelsViewModel(get()) }
    viewModel { NewRequestViewModel(get(), get()) }
    viewModel { TransferViewModel(get(), get())}
    viewModel { ScheduleDetailViewModel(get()) }
    viewModel { BalancesViewModel(get(), get())}
    viewModel { TransactionHistoryViewModel(get()) }
}

val dataModule = module {
    single { DatabaseRepo(get()) }
}