package com.hover.stax.database

import com.hover.sdk.database.HoverRoomDatabase
import com.hover.stax.accounts.AccountDetailViewModel
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.balances.BalancesViewModel
import com.hover.stax.bounties.BountyViewModel
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.faq.FaqViewModel
import com.hover.stax.futureTransactions.FutureViewModel
import com.hover.stax.inapp_banner.BannerViewModel
import com.hover.stax.languages.LanguageViewModel
import com.hover.stax.library.LibraryViewModel
import com.hover.stax.requests.NewRequestViewModel
import com.hover.stax.schedules.ScheduleDetailViewModel
import com.hover.stax.settings.SettingsViewModel
import com.hover.stax.transactions.TransactionDetailsViewModel
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.transfers.TransferViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { FaqViewModel() }
    viewModel { ActionSelectViewModel(get()) }
    viewModel { ChannelsViewModel(get(), get()) }
    viewModel { AccountDetailViewModel(get(), get()) }
    viewModel { NewRequestViewModel(get(), get()) }
    viewModel { TransferViewModel(get(), get()) }
    viewModel { ScheduleDetailViewModel(get()) }
    viewModel { BalancesViewModel(get(), get()) }
    viewModel { TransactionHistoryViewModel(get()) }
    viewModel { BannerViewModel(get(), get()) }
    viewModel { FutureViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { TransactionDetailsViewModel(get(), get()) }
    viewModel { LibraryViewModel(get(), get()) }
    viewModel { LanguageViewModel(get()) }
    viewModel { BountyViewModel(get(), get()) }
}

val dataModule = module(createdAtStart = true) {
    single { AppDatabase.getInstance(get()) }
    single { HoverRoomDatabase.getInstance(get()) }

    single { DatabaseRepo(get(), get()) }
}