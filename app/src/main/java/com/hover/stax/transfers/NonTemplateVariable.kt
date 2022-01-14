package com.hover.stax.transfers

import java.util.*

class NonTemplateVariable(val key: String,
                          var value: String? = null,
                          var editTextState: Int? = null) {

    companion object {
        fun getList(keys: LinkedList<String>) : LinkedList<NonTemplateVariable> {
            val nonTemplateVariables = LinkedList<NonTemplateVariable>()
            keys.forEach {
                nonTemplateVariables.add(NonTemplateVariable(it))
            }
            return nonTemplateVariables
        }
    }

}