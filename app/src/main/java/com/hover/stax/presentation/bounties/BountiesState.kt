package com.hover.stax.presentation.bounties

import com.hover.stax.domain.model.ChannelBounties

data class BountiesState(
    var loading: Boolean = false,
    var error: String = "",
    var bounties: List<ChannelBounties> = emptyList()
)