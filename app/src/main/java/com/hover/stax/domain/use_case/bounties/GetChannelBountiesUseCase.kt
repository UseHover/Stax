package com.hover.stax.domain.use_case.bounties

import com.hover.stax.countries.CountryAdapter
import com.hover.stax.domain.model.ChannelBounties
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.repository.BountyRepository
import com.hover.stax.domain.repository.ChannelRepository
import com.hover.stax.transactions.TransactionRepo
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetChannelBountiesUseCase(private val channelRepository: ChannelRepository, private val bountyRepository: BountyRepository, private val transactionRepo: TransactionRepo) {

    fun getBounties(countryCode: String = CountryAdapter.CODE_ALL_COUNTRIES): Flow<Resource<List<ChannelBounties>>> = flow {
        try {
            emit(Resource.Loading())

            emit(Resource.Success(fetchBounties(countryCode)))
        } catch (e: Exception) {
            emit(Resource.Error("Error loading bounties"))
        }
    }

    private suspend fun fetchBounties(countryCode: String): List<ChannelBounties> {
        val channelBounties: Deferred<List<ChannelBounties>>

        coroutineScope {
            channelBounties = async(Dispatchers.IO) {
                val bountyActions = bountyRepository.bountyActions
                val bountyTransactionList = transactionRepo.bountyTransactionList
                val bountyChannels = channelRepository.filterChannels(countryCode, bountyActions)

                bountyRepository.makeBounties(bountyActions, bountyTransactionList, bountyChannels)
            }
        }

        return channelBounties.await()
    }

    fun getChannelList(): Flow<List<String>> = bountyRepository.getCountryList()
}