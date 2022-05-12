package com.hover.stax.bonus

import com.hover.stax.database.AppDatabase

class BonusRepo(val db: AppDatabase) {

    private val dao = db.bonusDao()

    val bonuses = dao.bonuses

    fun save(bonus: Bonus) = dao.insert(bonus)

    fun save(bonuses: List<Bonus>) = dao.insertAll(bonuses)

    fun getBonus(recipientChannelId: Int) = dao.getBonuses(recipientChannelId)

    fun delete(bonus: Bonus) = dao.delete(bonus)

    fun delete() = dao.deleteAll()
}