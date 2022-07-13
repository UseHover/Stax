package com.hover.stax.data.local.bonus

import com.hover.stax.database.AppDatabase
import com.hover.stax.domain.model.Bonus

class BonusRepo(val db: AppDatabase) {

    private val dao = db.bonusDao()

    val bonuses = dao.bonuses

    fun save(bonus: Bonus) = dao.insert(bonus)

    fun save(bonuses: List<Bonus>) = dao.insertAll(bonuses)

    fun getBonusByPurchaseChannel(purchaseChannelId: Int): Bonus? = dao.getBonusByPurchaseChannel(purchaseChannelId)

    fun getBonusByUserChannel(userChannelId: Int): Bonus? = dao.getBonusByUserChannel(userChannelId)

    fun delete(bonus: Bonus) = dao.delete(bonus)

    fun delete() = dao.deleteAll()

    fun updateBonuses(bonuses: List<Bonus>) = dao.deleteAndSave(bonuses)
}