package com.hover.stax.domain.use_case

import com.hover.stax.bonus.Bonus
import com.hover.stax.data.Resource
import com.hover.stax.domain.repository.BonusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class GetBonusesUseCase(private val repository: BonusRepository) {

    fun getBonusList(): Flow<Resource<List<Bonus>>> = flow {
        emit(Resource.Loading())
        repository.getBonusList().collect {
            emit(Resource.Success(it))
        }
    }

    fun getBonusByPurchaseChannel(channelId: Int): Flow<Resource<Bonus>> = flow {
        emit(Resource.Loading())

        val bonus = repository.getBonusByPurchaseChannel(channelId)

        if(bonus != null)
            emit(Resource.Success(bonus))
        else
            emit(Resource.Error("Bonus not found"))
    }

    fun getBonusByUserChannel(channelId: Int): Flow<Resource<Bonus>> = flow {
        emit(Resource.Loading())

        val bonus = repository.getBonusByUserChannel(channelId)

        if(bonus != null)
            emit(Resource.Success(bonus))
        else
            emit(Resource.Error("Bonus not found"))
    }
}