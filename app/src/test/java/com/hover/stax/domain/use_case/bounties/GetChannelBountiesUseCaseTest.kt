package com.hover.stax.domain.use_case.bounties

import com.google.common.truth.Truth
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.sims.SimInfo
import com.hover.stax.channels.Channel
import com.hover.stax.domain.model.Bounty
import com.hover.stax.domain.model.ChannelBounties
import com.hover.stax.domain.repository.BountyRepository
import com.hover.stax.domain.repository.ChannelRepository
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.transactions.TransactionRepo
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class GetChannelBountiesUseCaseTest {
    private val bountyRepository = mockk<BountyRepository>(relaxed = true)
    private val channelRepository = mockk<ChannelRepository>(relaxed = true)
    private val transactionRepo = mockk<TransactionRepo>(relaxed = true)
    private val channelBounties = mockk<List<ChannelBounties>>(relaxed = true)
    private val bountyAction = mockk<Bounty>(relaxed = true)
    private val staxTransaction = mockk<List<StaxTransaction>>(relaxed = true)
    private val channelList = mockk<List<Channel>>(relaxed = true)
    private val countryCode = "KE"

    private lateinit var testSubject: GetChannelBountiesUseCase

    @Before
    fun setUp() {
        testSubject =
            GetChannelBountiesUseCase(channelRepository, bountyRepository, transactionRepo)
    }

    @Test
    fun `GetBounties should return success with channel bounties`() = runTest {
        val bountyActionsList = mockk<List<HoverAction>>(relaxed = true)

        coEvery { bountyRepository.bountyActions } returns bountyActionsList
        coEvery { transactionRepo.bountyTransactionList } returns staxTransaction
        coEvery {
            channelRepository.filterChannels(
                countryCode,
                bountyActionsList
            )
        } returns channelList
        coEvery {
            bountyRepository.makeBounties(
                bountyActionsList,
                staxTransaction,
                channelList
            )
        } returns channelBounties

        val result = testSubject.getBounties(countryCode).toList()
        Truth.assertThat(result.size).isEqualTo(2)
        Truth.assertThat(channelBounties).isEqualTo(result[1].data)
    }

    @Test
    fun `GetBounties should return Error when exception is thrown`() = runTest {
        val exception = RuntimeException("Something went wrong")
        coEvery { bountyRepository.bountyActions } throws exception
        val result = testSubject.getBounties(countryCode).toList()
        Truth.assertThat(result[1].message).isEqualTo("Error loading bounties")
    }

    @Test
    fun `when isSimPresent is called with empty sims then returns false`() = runTest {
        val sims = emptyList<SimInfo>()
        val result = testSubject.isSimPresent(bountyAction, sims)
        Truth.assertThat(result).isFalse()
    }

    @Test
    fun `getCountryList returns a list of country names `() = runTest {
        val countryList = listOf("Kenya", "Egypt", "Mali")
        coEvery { bountyRepository.getCountryList() } returns flowOf(countryList)
        val result = testSubject.getCountryList().toList().first()
        Truth.assertThat(countryList).isEqualTo(result)
    }
}