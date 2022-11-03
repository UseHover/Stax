package com.hover.stax.di

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.hover.sdk.database.HoverRoomDatabase
import com.hover.stax.BuildConfig
import com.hover.stax.R
import com.hover.stax.accounts.AccountDetailViewModel
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.data.local.SimRepo
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.data.local.bonus.BonusRepo
import com.hover.stax.data.local.channels.ChannelRepo
import com.hover.stax.data.local.parser.ParserRepo
import com.hover.stax.data.local.user.UserRepo
import com.hover.stax.data.remote.auth.StaxApi
import com.hover.stax.data.repository.AccountRepositoryImpl
import com.hover.stax.data.repository.AuthRepositoryImpl
import com.hover.stax.data.repository.BonusRepositoryImpl
import com.hover.stax.data.repository.BountyRepositoryImpl
import com.hover.stax.data.repository.ChannelRepositoryImpl
import com.hover.stax.data.repository.FinancialTipsRepositoryImpl
import com.hover.stax.data.repository.SimRepositoryImpl
import com.hover.stax.data.repository.StaxUserRepositoryImpl
import com.hover.stax.database.AppDatabase
import com.hover.stax.domain.repository.AccountRepository
import com.hover.stax.domain.repository.AuthRepository
import com.hover.stax.domain.repository.BonusRepository
import com.hover.stax.domain.repository.BountyRepository
import com.hover.stax.domain.repository.ChannelRepository
import com.hover.stax.domain.repository.FinancialTipsRepository
import com.hover.stax.domain.repository.SimRepository
import com.hover.stax.domain.repository.StaxUserRepository
import com.hover.stax.domain.use_case.accounts.CreateAccountsUseCase
import com.hover.stax.domain.use_case.accounts.GetAccountsUseCase
import com.hover.stax.domain.use_case.accounts.SetDefaultAccountUseCase
import com.hover.stax.domain.use_case.auth.AuthUseCase
import com.hover.stax.domain.use_case.bonus.GetBonusesUseCase
import com.hover.stax.domain.use_case.bonus.RefreshBonusUseCase
import com.hover.stax.domain.use_case.bounties.GetChannelBountiesUseCase
import com.hover.stax.domain.use_case.financial_tips.TipsUseCase
import com.hover.stax.domain.use_case.sims.GetPresentSimUseCase
import com.hover.stax.domain.use_case.stax_user.StaxUserUseCase
import com.hover.stax.faq.FaqViewModel
import com.hover.stax.futureTransactions.FutureViewModel
import com.hover.stax.inapp_banner.BannerViewModel
import com.hover.stax.ktor.KtorClientFactory
import com.hover.stax.languages.LanguageViewModel
import com.hover.stax.login.LoginViewModel
import com.hover.stax.merchants.MerchantRepo
import com.hover.stax.merchants.MerchantViewModel
import com.hover.stax.paybill.PaybillRepo
import com.hover.stax.paybill.PaybillViewModel
import com.hover.stax.preferences.DefaultTokenProvider
import com.hover.stax.preferences.TokenProvider
import com.hover.stax.presentation.bounties.BountyViewModel
import com.hover.stax.presentation.financial_tips.FinancialTipsViewModel
import com.hover.stax.presentation.home.BalancesViewModel
import com.hover.stax.presentation.home.HomeViewModel
import com.hover.stax.presentation.sim.SimViewModel
import com.hover.stax.requests.NewRequestViewModel
import com.hover.stax.requests.RequestDetailViewModel
import com.hover.stax.requests.RequestRepo
import com.hover.stax.schedules.ScheduleDetailViewModel
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.transactionDetails.TransactionDetailsViewModel
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.transactions.TransactionRepo
import com.hover.stax.transfers.TransferViewModel
import io.ktor.client.engine.android.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    singleOf(::SimRepo)
}

val ktorModule = module {
    single {
        KtorClientFactory(get()).create(Android.create {
            connectTimeout = 10_000
        })
    }
//    single { StaxApi(get()) }
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
                    sharedPreferencesName = "stax-pref"
                )
            ),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { androidContext().preferencesDataStoreFile(name = "stax-pref") }
        )
    }
}

val networkModule = module {
    single<StaxApi> {
        val loggingInterceptor =
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val okHttpClient = OkHttpClient()
            .newBuilder()
//            .authenticator(TokenAuthenticator())
//            .addInterceptor(TokenInterceptor())

        if (BuildConfig.DEBUG)
            okHttpClient.addInterceptor(loggingInterceptor)

        Retrofit.Builder()
            .baseUrl(androidContext().resources.getString(R.string.root_url))
            .client(okHttpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StaxApi::class.java)
    }
}

val repositories = module {
    single(named("CoroutineDispatcher")) {
        Dispatchers.IO
    }

    single<TokenProvider> { DefaultTokenProvider(get()) }

    single<BonusRepository> { BonusRepositoryImpl(get(), get()) }
    single<AccountRepository> { AccountRepositoryImpl(get(), get(), get()) }
    single<BountyRepository> { BountyRepositoryImpl(get(), get(named("CoroutineDispatcher"))) }
    single<SimRepository> { SimRepositoryImpl(get()) }

    singleOf(::FinancialTipsRepositoryImpl) { bind<FinancialTipsRepository>() }
    singleOf(::ChannelRepositoryImpl) { bind<ChannelRepository>() }
    singleOf(::StaxUserRepositoryImpl) { bind<StaxUserRepository>() }
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
}

val useCases = module {
    factoryOf(::GetBonusesUseCase)
    factoryOf(::RefreshBonusUseCase)

    factoryOf(::GetAccountsUseCase)
    factoryOf(::SetDefaultAccountUseCase)
    factoryOf(::CreateAccountsUseCase)

    factoryOf(::TipsUseCase)

    factoryOf(::GetChannelBountiesUseCase)
    factoryOf(::GetPresentSimUseCase)

    factoryOf(::StaxUserUseCase)
    factoryOf(::AuthUseCase)
}