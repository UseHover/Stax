package com.hover.stax.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.database.channel.repository.ChannelRepository
import com.hover.stax.domain.model.Account
import com.hover.stax.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AccountRepositoryTest {

    @get:Rule
    var mainCoroutineRule = MainDispatcherRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val accountRepo = mockk<AccountRepo>(relaxed = true)
    private val actionRepo = mockk<ActionRepo>(relaxed = true)
    private val channelRepo = mockk<ChannelRepository>(relaxed = true)
    private lateinit var testSubject: AccountRepositoryImpl

    @Before
    fun setup() {
        testSubject = AccountRepositoryImpl(
            actionRepo = actionRepo,
            channelRepository = channelRepo ,
            accountRepo = accountRepo
        )
    }

    @Test
    fun `verify That accounts are fetched from the Accounts Repo`() = runTest {
        val mockAccount = mockk<Account>()
        coEvery { accountRepo.getAccountBySim(any()) } returns mockAccount

        testSubject.getAccountBySim(1)

        coVerify {
            accountRepo.getAccountBySim(any())
        }

    }
    // Fails because of the Sim Info class which is a part of the Hover SDK
//    @Test
//    fun `if channel is not null the account is inserted in the account Repo`() = runTest {
//        val mockSim = mockk<SimInfo>(relaxed = true)
//        val mockChannel = mockk<Channel>()
//        val mockAccount = mockk<Account>()
//        coEvery { channelRepo.getTelecom(any()) } returns mockChannel
//
//        testSubject.createAccount(sim = mockSim)
//        coVerify {
//            accountRepo.insert(account = any())
//        }
//
//
//    }




}