package com.hover.stax.domain.use_case.channels

import com.hover.sdk.sims.SimInfo
import com.hover.stax.domain.model.Bounty
import com.hover.stax.domain.repository.BountyRepository
import com.hover.stax.domain.repository.ChannelRepository

class GetPresentSimsUseCase(channelRepository: ChannelRepository, private val bountyRepository: BountyRepository) {

    val presentSims: List<SimInfo> = channelRepository.presentSims

    fun simPresent(bounty: Bounty, sims: List<SimInfo>): Boolean = bountyRepository.simPresent(bounty, sims)

}