package com.hover.stax.database

import com.hover.sdk.database.HoverRoomDatabase
import com.hover.stax.accounts.AccountDetailViewModel
import com.hover.stax.accounts.AccountRepo
import com.hover.stax.actions.ActionRepo
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.balances.BalancesViewModel
import com.hover.stax.bonus.BonusRepo
import com.hover.stax.bonus.BonusViewModel
import com.hover.stax.bounties.BountyViewModel
import com.hover.stax.channels.ChannelRepo
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.faq.FaqViewModel
import com.hover.stax.financialTips.FinancialTipsViewModel
import com.hover.stax.futureTransactions.FutureViewModel
import com.hover.stax.inapp_banner.BannerViewModel
import com.hover.stax.languages.LanguageViewModel
import com.hover.stax.login.LoginNetworking
import com.hover.stax.login.LoginViewModel
import com.hover.stax.paybill.PaybillRepo
import com.hover.stax.paybill.PaybillViewModel
import com.hover.stax.requests.NewRequestViewModel
import com.hover.stax.requests.RequestDetailViewModel
import com.hover.stax.requests.RequestRepo
import com.hover.stax.schedules.ScheduleDetailViewModel
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.transactionDetails.TransactionDetailsViewModel
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.transactions.TransactionRepo
import com.hover.stax.transfers.TransferViewModel
import com.hover.stax.user.UserRepo
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { FaqViewModel() }
    viewModel { ActionSelectViewModel(get()) }
    viewModel { ChannelsViewModel(get(), get(), get(), get()) }
    viewModel { AccountsViewModel(get(), get(), get()) }
    viewModel { AccountDetailViewModel(get(), get(), get(), get(), get()) }
    viewModel { NewRequestViewModel(get(), get(), get(), get(), get()) }
    viewModel { TransferViewModel(get(), get(), get(), get()) }
    viewModel { ScheduleDetailViewModel(get(), get(), get()) }
    viewModel { BalancesViewModel(get(), get()) }
    viewModel { TransactionHistoryViewModel(get()) }
    viewModel { BannerViewModel(get(), get()) }
    viewModel { FutureViewModel(get(), get(), get()) }
    viewModel { LoginViewModel(get(), get(), get())}
    viewModel { TransactionDetailsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { LanguageViewModel(get()) }
    viewModel { BountyViewModel(get(), get(), get(), get()) }
    viewModel { FinancialTipsViewModel() }
    viewModel { PaybillViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { RequestDetailViewModel(get(), get(), get()) }
    viewModel { BonusViewModel(get(), get()) }

}

val dataModule = module(createdAtStart = true) {
    single { AppDatabase.getInstance(get()) }
    single { HoverRoomDatabase.getInstance(get()) }

    single { TransactionRepo(get()) }
    single { ChannelRepo(get(), get()) }
    single { ActionRepo(get()) }
    single { ContactRepo(get()) }
    single { AccountRepo(get()) }
    single { RequestRepo(get()) }
    single { ScheduleRepo(get()) }
    single { PaybillRepo(get()) }
    single { UserRepo(get()) }
    single { BonusRepo(get()) }
    single { ParserRepo(get()) }
}

val networkModule = module {
    single { LoginNetworking(get()) }
}