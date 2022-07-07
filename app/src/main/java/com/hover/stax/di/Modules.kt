package com.hover.stax.di

import com.hover.sdk.database.HoverRoomDatabase
import com.hover.stax.accounts.AccountDetailViewModel
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.actions.ActionRepo
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.presentation.home.BalancesViewModel
import com.hover.stax.bonus.BonusViewModel
import com.hover.stax.bounties.BountyViewModel
import com.hover.stax.channels.ChannelRepo
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.bonus.BonusRepo
import com.hover.stax.data.repository.AccountRepositoryImpl
import com.hover.stax.data.repository.BonusRepositoryImpl
import com.hover.stax.data.repository.FinancialTipsRepositoryImpl
import com.hover.stax.database.AppDatabase
import com.hover.stax.database.ParserRepo
import com.hover.stax.domain.repository.AccountRepository
import com.hover.stax.domain.repository.BonusRepository
import com.hover.stax.domain.repository.FinancialTipsRepository
import com.hover.stax.domain.use_case.accounts.CreateAccountsUseCase
import com.hover.stax.domain.use_case.accounts.GetAccountsUseCase
import com.hover.stax.domain.use_case.accounts.SetDefaultAccountUseCase
import com.hover.stax.domain.use_case.bonus.FetchBonusUseCase
import com.hover.stax.domain.use_case.bonus.GetBonusesUseCase
import com.hover.stax.domain.use_case.financial_tips.GetTipsUseCase
import com.hover.stax.faq.FaqViewModel
import com.hover.stax.presentation.financial_tips.FinancialTipsViewModel
import com.hover.stax.presentation.home.HomeViewModel
import com.hover.stax.futureTransactions.FutureViewModel
import com.hover.stax.inapp_banner.BannerViewModel
import com.hover.stax.languages.LanguageViewModel
import com.hover.stax.login.LoginNetworking
import com.hover.stax.login.LoginViewModel
import com.hover.stax.merchants.MerchantRepo
import com.hover.stax.merchants.MerchantViewModel
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
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::FaqViewModel)
    viewModelOf(::ActionSelectViewModel)
    viewModelOf(::ChannelsViewModel)
    viewModelOf(::AccountsViewModel)
    viewModelOf(::AccountDetailViewModel)
    viewModelOf(::NewRequestViewModel)
    viewModelOf(::TransferViewModel)
    viewModelOf(::ScheduleDetailViewModel)
    viewModelOf(::BalancesViewModel)
    viewModelOf(::TransactionHistoryViewModel)
    viewModelOf(::BannerViewModel)
    viewModelOf(::FutureViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::TransactionDetailsViewModel)
    viewModelOf(::LanguageViewModel)
    viewModelOf(::BountyViewModel)
    viewModelOf(::FinancialTipsViewModel)
    viewModelOf(::PaybillViewModel)
    viewModelOf(::MerchantViewModel)
    viewModelOf(::RequestDetailViewModel)
    viewModelOf(::BonusViewModel)

    viewModelOf(::HomeViewModel)
}

val dataModule = module(createdAtStart = true) {
    single { AppDatabase.getInstance(get()) }
    single { HoverRoomDatabase.getInstance(get()) }

    singleOf(::TransactionRepo)
    singleOf(::ChannelRepo)
    singleOf(::ActionRepo)
    singleOf(::ContactRepo)
    singleOf(::AccountRepo)
    singleOf(::RequestRepo)
    singleOf(::ScheduleRepo)
    singleOf(::PaybillRepo)
    singleOf(::MerchantRepo)
    singleOf(::UserRepo)
    singleOf(::BonusRepo)
    singleOf(::ParserRepo)
}

val networkModule = module {
    singleOf(::LoginNetworking)
}

val repositories = module {
    single(named("CoroutineDispatcher")) {
        Dispatchers.IO
    }

    single<BonusRepository> { BonusRepositoryImpl(get(), get(), get(named("CoroutineDispatcher"))) }
    single<AccountRepository> { AccountRepositoryImpl(get(), get(), get(), get(named("CoroutineDispatcher"))) }
    single<FinancialTipsRepository> { FinancialTipsRepositoryImpl(get())}
}

val useCases = module {
    factoryOf(::GetBonusesUseCase)
    factoryOf(::FetchBonusUseCase)

    factoryOf(::GetAccountsUseCase)
    factoryOf(::SetDefaultAccountUseCase)
    factoryOf(::CreateAccountsUseCase)

    factoryOf(::GetTipsUseCase)
}