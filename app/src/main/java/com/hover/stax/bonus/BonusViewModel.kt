package com.hover.stax.bonus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelRepo
import com.hover.stax.utils.toHni
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class BonusViewModel(val repo: BonusRepo, private val channelRepo: ChannelRepo) : ViewModel() {

    private val _bonusList = MutableStateFlow<List<Bonus>>(emptyList())
    val bonuses: StateFlow<List<Bonus>> = _bonusList

    private val db = Firebase.firestore
    private val settings = firestoreSettings { isPersistenceEnabled = true }

    init {
        db.firestoreSettings = settings
    }

    fun fetchBonuses() = viewModelScope.launch(Dispatchers.IO) {
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
                saveBonuses(results)
            }
            .addOnFailureListener {
                Timber.e("Error fetching bonuses: ${it.localizedMessage}")
                _bonusList.value = emptyList()
            }
    }

    fun getBonusList() = viewModelScope.launch(Dispatchers.IO) {
        repo.bonuses.collect { _bonusList.value = it }
    }

    private fun saveBonuses(bonuses: List<Bonus>) = viewModelScope.launch(Dispatchers.IO) {
        val simHnis = channelRepo.presentSims.map { it.osReportedHni }
        val bonusChannels = channelRepo.getChannelsByIds(bonuses.map { it.purchaseChannel })

        val toSave = bonuses.filter { bonusChannels.map { channel -> channel.id }.contains(it.purchaseChannel) }
        repo.updateBonuses(toSave)

        val showBonuses = hasValidSim(simHnis, bonusChannels)
        _bonusList.value = if (showBonuses) toSave else emptyList()
    }

    fun getBonusByPurchaseChannel(channelId: Int): Bonus? = repo.getBonusByPurchaseChannel(channelId)

    fun getBonusByUserChannel(channelId: Int): Bonus? = repo.getBonusByUserChannel(channelId)

    /**
     * Extract the hnis from the bonus channels and compare with current sim hnis.
     * Return true if user has a valid sim
     */
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
}