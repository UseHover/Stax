package com.hover.stax.di

import com.hover.sdk.database.HoverRoomDatabase
import com.hover.stax.R
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.balances.BalancesViewModel
import com.hover.stax.channels.ChannelDetailViewModel
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.database.AppDatabase
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.faq.FaqViewModel
import com.hover.stax.futureTransactions.FutureViewModel
import com.hover.stax.inapp_banner.BannerViewModel
import com.hover.stax.requests.NewRequestViewModel
import com.hover.stax.schedules.ScheduleDetailViewModel
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.transfers.TransferViewModel
import com.hover.stax.utils.network.NetworkMonitor
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { FaqViewModel() }
    viewModel { ActionSelectViewModel(get()) }
    viewModel { ChannelsViewModel(get()) }
    viewModel { ChannelDetailViewModel(get()) }
    viewModel { NewRequestViewModel(get(), get()) }
    viewModel { TransferViewModel(get(), get()) }
    viewModel { ScheduleDetailViewModel(get()) }
    viewModel { BalancesViewModel(get(), get()) }
    viewModel { TransactionHistoryViewModel(get()) }
    viewModel { BannerViewModel(get(), get()) }
    viewModel { FutureViewModel(get()) }
}

val dataModule = module(createdAtStart = true) {
    single { AppDatabase.getInstance(get()) }
    single { HoverRoomDatabase.getInstance(get()) }

    single { DatabaseRepo(get(), get()) }
}

val utilsModule = module(createdAtStart = true) {
    single { MixpanelAPI.getInstance(get(), androidContext().getString(R.string.mixpanel_token)) }
    single { NetworkMonitor(get()) }
}