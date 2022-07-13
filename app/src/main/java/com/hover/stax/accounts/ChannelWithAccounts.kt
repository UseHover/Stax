package com.hover.stax.accounts

import androidx.room.Embedded
import androidx.room.Relation
import com.hover.stax.channels.Channel

data class ChannelWithAccounts(
        @Embedded
        val channel: Channel,

        @Relation(
                parentColumn = "id",
                entityColumn = "channelId"
        )
        val accounts: List<Account>
)
