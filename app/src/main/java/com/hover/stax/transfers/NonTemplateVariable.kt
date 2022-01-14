package com.hover.stax.transfers

import java.util.*

data class NonTemplateVariable(val key: String,
                          var value: String? = null,
                          var editTextState: Int? = null) {

    companion object {
        fun getList(keys: List<String>) : List<NonTemplateVariable> {
            val nonTemplateVariables = ArrayList<NonTemplateVariable>()
            keys.forEach {
                nonTemplateVariables.add(NonTemplateVariable(it))
            }
            return nonTemplateVariables
        }
    }

}