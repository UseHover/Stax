package com.hover.stax.data.repository

import com.hover.stax.data.local.SimRepo
import com.hover.stax.data.local.bonus.BonusRepo
import com.hover.stax.data.remote.StaxFirebase
import com.hover.stax.domain.model.Bonus
import com.hover.stax.domain.repository.BonusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import timber.log.Timber

class BonusRepositoryImpl(
	private val bonusRepo: BonusRepo,
	private val simRepo: SimRepo,
) : BonusRepository {

	override suspend fun refreshBonuses() {
		try {
			val staxFirebase = StaxFirebase()
			clearCacheIfRequired(staxFirebase) // To forces users to fetch new data online if the cached ones does not contains HNI
			val allBonuses = staxFirebase.fetchBonuses().documents.map { Bonus(it) }
			bonusRepo.update(allBonuses)
		} catch (e: Exception) {
			Timber.e("Bonus: Error fetching bonuses: ${e.localizedMessage}")
		}
	}

	override val bonusList: Flow<List<Bonus>>
		get() = channelFlow {
			val simHniList = simRepo.getPresentSims().map { it.osReportedHni }
			bonusRepo.bonuses.collect { send(simSupportedBonuses(simHniList, it)) }
		}

	override suspend fun saveBonuses(bonusList: List<Bonus>) {
		return bonusRepo.save(bonusList)
	}

	override suspend fun getBonusByUserChannel(channelId: Int): Bonus? {
		return bonusRepo.getBonusByUserChannel(channelId)
	}

	private suspend fun clearCacheIfRequired(staxFirebase: StaxFirebase) {
		val validLocalBonuses = bonusRepo.bonusCountWithHni()
		if(validLocalBonuses == 0)  staxFirebase.clearPersistence()
	}

	private fun simSupportedBonuses(simHnis: List<String>, bonuses: List<Bonus>): List<Bonus> {
		val resultBonuses = mutableListOf<Bonus>()
		bonuses.forEach { bonus ->
			bonus.hniList.split(",").forEach {
				if (simHnis.contains(it)) resultBonuses.add(bonus)
			}
		}
		return resultBonuses
	}
}