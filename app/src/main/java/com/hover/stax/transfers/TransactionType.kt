package com.hover.stax.transfers

import timber.log.Timber
import kotlin.properties.Delegates

class TransactionType {

    companion object {
        var type: String by Delegates.observable("P2P") { _, oldValue, newValue ->
            Timber.e("$oldValue -> $newValue")
        }
    }
}