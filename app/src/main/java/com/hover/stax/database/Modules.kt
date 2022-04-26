package com.hover.stax.database

import com.hover.sdk.database.HoverRoomDatabase
import com.hover.stax.accounts.AccountDetailViewModel
import com.hover.stax.accounts.AccountRepo
import com.hover.stax.actions.ActionRepo
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.addChannels.AddChannelsViewModel
import com.hover.stax.balances.BalancesViewModel
import com.hover.stax.bounties.BountyViewModel
import com.hover.stax.channels.ChannelRepo
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.faq.FaqViewModel
import com.hover.stax.financialTips.FinancialTipsViewModel
import com.hover.stax.futureTransactions.FutureViewModel
import com.hover.stax.hover.HoverViewModel
import com.hover.stax.inapp_banner.BannerViewModel
import com.hover.stax.languages.LanguageViewModel
import com.hover.stax.ussd_library.LibraryViewModel
import com.hover.stax.login.LoginViewModel
import com.hover.stax.paybill.PaybillRepo
import com.hover.stax.paybill.PaybillViewModel
import com.hover.stax.requests.NewRequestViewModel
import com.hover.stax.requests.RequestDetailViewModel
import com.hover.stax.requests.RequestRepo
import com.hover.stax.schedules.ScheduleDetailViewModel
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.transactions.TransactionDetailsViewModel
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.transactions.TransactionRepo
import com.hover.stax.transfers.TransferViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { FaqViewModel() }
    viewModel { HoverViewModel(get(), get(), get()) }
    viewModel { ActionSelectViewModel(get()) }
    viewModel { AddChannelsViewModel(get(), get(), get(), get()) }
    viewModel { ChannelsViewModel(get(), get(), get(), get()) }
    viewModel { AccountDetailViewModel(get(), get(), get(), get(), get()) }
    viewModel { NewRequestViewModel(get(), get(), get(), get(), get()) }
    viewModel { TransferViewModel(get(), get(), get(), get()) }
    viewModel { ScheduleDetailViewModel(get(), get(), get()) }
    viewModel { BalancesViewModel(get(), get(), get()) }
    viewModel { TransactionHistoryViewModel(get()) }
    viewModel { BannerViewModel(get(), get()) }
    viewModel { FutureViewModel(get(), get(), get()) }
    viewModel { LoginViewModel(get(), get())}
    viewModel { TransactionDetailsViewModel(get(), get(), get(), get(), get()) }
    viewModel { LibraryViewModel(get(), get()) }
    viewModel { LanguageViewModel(get()) }
    viewModel { BountyViewModel(get(), get(), get(), get()) }
    viewModel { FinancialTipsViewModel() }
    viewModel { PaybillViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { RequestDetailViewModel(get(), get(), get()) }
}

val dataModule = module(createdAtStart = true) {
    single { AppDatabase.getInstance(get()) }
    single { HoverRoomDatabase.getInstance(get()) }

    single { TransactionRepo(get(), get()) }
    single { ChannelRepo(get(), get()) }
    single { ActionRepo(get()) }
    single { ContactRepo(get(), get()) }
    single { AccountRepo(get(), get()) }
    single { RequestRepo(get(), get()) }
    single { ScheduleRepo(get()) }
    single { PaybillRepo(get()) }
}