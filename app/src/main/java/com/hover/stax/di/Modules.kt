/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.di

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.hover.sdk.database.HoverRoomDatabase
import com.hover.stax.accounts.AccountDetailViewModel
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.data.local.SimRepo
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.data.local.parser.ParserRepo
import com.hover.stax.data.remote.StaxApi
import com.hover.stax.data.repository.AccountRepositoryImpl
import com.hover.stax.data.repository.AuthRepositoryImpl
import com.hover.stax.data.repository.BountyRepositoryImpl
import com.hover.stax.data.repository.FinancialTipsRepositoryImpl
import com.hover.stax.database.AppDatabase
import com.hover.stax.domain.repository.AccountRepository
import com.hover.stax.domain.repository.AuthRepository
import com.hover.stax.domain.repository.BountyRepository
import com.hover.stax.domain.repository.FinancialTipsRepository
import com.hover.stax.domain.use_case.bounties.GetChannelBountiesUseCase
import com.hover.stax.domain.use_case.financial_tips.TipsUseCase
import com.hover.stax.domain.use_case.sims.ListSimsUseCase
import com.hover.stax.domain.use_case.stax_user.StaxUserUseCase
import com.hover.stax.faq.FaqViewModel
import com.hover.stax.futureTransactions.FutureViewModel
import com.hover.stax.inapp_banner.BannerViewModel
import com.hover.stax.ktor.EnvironmentProvider
import com.hover.stax.ktor.KtorClientFactory
import com.hover.stax.languages.LanguageViewModel
import com.hover.stax.login.LoginViewModel
import com.hover.stax.merchants.MerchantRepo
import com.hover.stax.merchants.MerchantViewModel
import com.hover.stax.paybill.PaybillRepo
import com.hover.stax.paybill.PaybillViewModel
import com.hover.stax.preferences.DefaultSharedPreferences
import com.hover.stax.preferences.DefaultTokenProvider
import com.hover.stax.preferences.LocalPreferences
import com.hover.stax.preferences.TokenProvider
import com.hover.stax.presentation.bounties.BountyViewModel
import com.hover.stax.presentation.financial_tips.FinancialTipsViewModel
import com.hover.stax.presentation.home.BalancesViewModel
import com.hover.stax.presentation.home.HomeViewModel
import com.hover.stax.presentation.sims.SimViewModel
import com.hover.stax.requests.NewRequestViewModel
import com.hover.stax.requests.RequestDetailViewModel
import com.hover.stax.requests.RequestRepo
import com.hover.stax.schedules.ScheduleDetailViewModel
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.transactionDetails.TransactionDetailsViewModel
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.transactions.TransactionRepo
import com.hover.stax.transfers.TransferViewModel
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val TIMEOUT = 10_000

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

    viewModelOf(::HomeViewModel)
    viewModelOf(::SimViewModel)
}

val dataModule = module(createdAtStart = true) {
    single { AppDatabase.getInstance(get()) }
    single { HoverRoomDatabase.getInstance(get()) }
    single { get<AppDatabase>().userDao() }

    singleOf(::TransactionRepo)
    singleOf(::ActionRepo)
    singleOf(::ContactRepo)
    singleOf(::AccountRepo)
    singleOf(::RequestRepo)
    singleOf(::ScheduleRepo)
    singleOf(::PaybillRepo)
    singleOf(::MerchantRepo)
    singleOf(::ParserRepo)
    singleOf(::SimRepo)

    singleOf(::StaxApi)
}

val ktorModule = module {

    single { EnvironmentProvider(androidApplication(), get()) }

    single {
        KtorClientFactory(get(), get()).create(
            Android.create {
                connectTimeout = TIMEOUT
            }
        )
    }
}

val datastoreModule = module {
    single {
        PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(
                SharedPreferencesMigration(
                    androidContext(),
                    sharedPreferencesName = "stax.datastore"
                )
            ),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { androidContext().preferencesDataStoreFile(name = "stax.datastore") }
        )
    }
}

val repositories = module {
    single(named("CoroutineDispatcher")) {
        Dispatchers.IO
    }

    single<TokenProvider> { DefaultTokenProvider(get()) }
    single<LocalPreferences> { DefaultSharedPreferences(androidApplication()) }

    single<AccountRepository> { AccountRepositoryImpl(get(), get(), get()) }
    single<BountyRepository> { BountyRepositoryImpl(get(), get(named("CoroutineDispatcher"))) }

    singleOf(::FinancialTipsRepositoryImpl) { bind<FinancialTipsRepository>() }

    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
}

val useCases = module {
    single(named("CoroutineDispatcher")) {
        Dispatchers.IO
    }
    single { ListSimsUseCase(get(), get(), get(), get(named("CoroutineDispatcher"))) }

    factoryOf(::TipsUseCase)

    factoryOf(::GetChannelBountiesUseCase)

    factoryOf(::StaxUserUseCase)
}