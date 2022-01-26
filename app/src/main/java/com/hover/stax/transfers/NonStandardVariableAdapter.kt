package com.hover.stax.transfers

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.R
import com.hover.stax.databinding.NonStandardVariableItemBinding
import com.hover.stax.views.AbstractStatefulInput

class NonStandardVariableAdapter(private var variables: LinkedHashMap<String, String>,
                                 private val editTextListener: NonStandardVariableInputListener,
                                 private val runValidation: Boolean) :
        RecyclerView.Adapter<NonStandardVariableAdapter.ViewHolder>() {

    var seenAnError: Boolean? = null
    var entryValidationCount = 0;

    inner class ViewHolder(val binding: NonStandardVariableItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bindItems(key: String, value: String) {

            val watcher: TextWatcher = object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    val trimmedValue = charSequence.toString().replace(",".toRegex(), "")
                    editTextListener.nonStandardVariableInputUpdated(key, trimmedValue)
                }
            }

            binding.variableInput.setHint(key)
            binding.variableInput.tag = key
            binding.variableInput.addTextChangedListener(watcher)

            if(runValidation) {
                binding.variableInput.text = value
                val ctx : Context = binding.root.context
                if(value.isEmpty()) {
                    updateValidationStatus(hasError = true)
                    val message = ctx.getString(R.string.enterValue_non_template_error, key).lowercase()
                    binding.variableInput.setState(message, AbstractStatefulInput.ERROR)
                }
                else {
                    updateValidationStatus(hasError = false)
                    binding.variableInput.setState(null, AbstractStatefulInput.SUCCESS)
                }
            }
        }
    }

    private fun updateValidationStatus(hasError: Boolean) {
        entryValidationCount += 1
        if(seenAnError == null || seenAnError == false) seenAnError = hasError

        //Ensure validation result is returned after all entry has been checked
        if(entryValidationCount == variables.size){
            editTextListener.validateFormEntries(hasError)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NonStandardVariableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        variables.onEachIndexed{
            index, entry ->
            if(index == position)  holder.bindItems(entry.key, entry.value)
        }

    }

    interface NonStandardVariableInputListener {
        fun nonStandardVariableInputUpdated(key: String, value: String)
        fun validateFormEntries(nonStandardHasAnError: Boolean)
    }


    override fun getItemCount(): Int {
     return variables.size
    }
}