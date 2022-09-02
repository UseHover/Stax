package com.hover.stax.data.repository

import com.google.firebase.firestore.QuerySnapshot
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
		Timber.i("Bonus: Called on refresh bonuses")
		val firebaseOperation = StaxFirebase().fetchBonuses()
		if (firebaseOperation.isComplete && firebaseOperation.isSuccessful) {
			val allBonuses = firebaseOperation.result.map { Bonus(it) }
			bonusRepo.update(allBonuses)
			Timber.i("Bonus: Saved ${allBonuses.size} bonuses")
		} else if (firebaseOperation.isComplete && !firebaseOperation.isSuccessful) {
			Timber.e("Bonus: Error fetching bonuses: ${firebaseOperation.exception?.localizedMessage}")
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