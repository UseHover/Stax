package com.hover.stax.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.hover.stax.bonus.Bonus
import com.hover.stax.bonus.BonusRepo
import com.hover.stax.domain.repository.BonusRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class BonusRepositoryImpl(private val bonusRepo: BonusRepo, private val coroutineDispatcher: CoroutineDispatcher) : BonusRepository {

    private val settings = firestoreSettings { isPersistenceEnabled = true }
    private val db = Firebase.firestore.also { it.firestoreSettings = settings }

    override suspend fun fetchBonuses() {
        withContext(coroutineDispatcher) {
            db.collection("bonuses")
                .get()
                .addOnSuccessListener { snapshot ->
                    val results = snapshot.map { document ->
                        Bonus(
                            document.data["user_channel"].toString().toInt(), document.data["purchase_channel"].toString().toInt(),
                            document.data["bonus_percent"].toString().toDouble(), document.data["message"].toString()
                        )
                    }

                    Timber.e("Saved ${results.size} bonuses")
                    launch {
                        saveBonuses(results)
                    }
                }
                .addOnFailureListener {
                    Timber.e("Error fetching bonuses: ${it.localizedMessage}")
                }
        }
    }

    override suspend fun getBonusList(): Flow<List<Bonus>> {
        return bonusRepo.bonuses
    }

    override suspend fun saveBonuses(bonusList: List<Bonus>) {
        return bonusRepo.save(bonusList)
    }

    override suspend fun getBonusByPurchaseChannel(channelId: Int): Bonus? {
        return bonusRepo.getBonusByPurchaseChannel(channelId)
    }

    override suspend fun getBonusByUserChannel(channelId: Int): Bonus? {
        return bonusRepo.getBonusByUserChannel(channelId)
    }
}