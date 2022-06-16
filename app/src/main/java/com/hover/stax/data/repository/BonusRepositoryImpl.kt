package com.hover.stax.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.hover.stax.domain.model.Bonus
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelRepo
import com.hover.stax.data.local.bonus.BonusRepo
import com.hover.stax.domain.repository.BonusRepository
import com.hover.stax.utils.toHni
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class BonusRepositoryImpl(private val bonusRepo: BonusRepo, private val channelRepo: ChannelRepo, private val coroutineDispatcher: CoroutineDispatcher) : BonusRepository {

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
                        filterResults(results)
                    }
                }
                .addOnFailureListener {
                    Timber.e("Error fetching bonuses: ${it.localizedMessage}")
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getBonusList(): Flow<List<Bonus>> = channelFlow {
        val simHnis = channelRepo.presentSims.map { it.osReportedHni }

        bonusRepo.bonuses.collect {
            withContext(coroutineDispatcher) {
                launch {
                    val bonusChannels = getBonusChannels(it)
                    val showBonuses = hasValidSim(simHnis, bonusChannels)

                    if (showBonuses)
                        send(it)
                    else
                        send(emptyList())
                }
            }
        }
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

    private suspend fun filterResults(bonuses: List<Bonus>) = withContext(coroutineDispatcher) {
        launch {
            val bonusChannels = getBonusChannels(bonuses)

            val toSave = bonuses.filter { bonusChannels.map { channel -> channel.id }.contains(it.purchaseChannel) }
            bonusRepo.updateBonuses(toSave)
        }
    }

    private fun hasValidSim(simHnis: List<String>, bonusChannels: List<Channel>): Boolean {
        val hniList = mutableSetOf<String>()
        bonusChannels.forEach { channel ->
            channel.hniList.split(",").forEach {
                if (simHnis.contains(it.toHni()))
                    hniList.add(it.toHni())
            }
        }

        return hniList.isNotEmpty()
    }

    override suspend fun getBonusChannels(bonusList: List<Bonus>): List<Channel> = channelRepo.getChannelsByIdsAsync(bonusList.map { it.purchaseChannel })
}