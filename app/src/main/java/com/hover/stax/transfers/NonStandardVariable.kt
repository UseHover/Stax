package com.hover.stax.transfers

import java.util.*

data class NonStandardVariable(val key: String,
                               var value: String? = null,
                               var editTextState: Int? = null) {

    companion object {
        fun getList(keys: List<String>) : List<NonStandardVariable> {
            val nonStandardVariables = ArrayList<NonStandardVariable>()
            keys.forEach {
                nonStandardVariables.add(NonStandardVariable(it))
            }
            return nonStandardVariables
        }
    }

}