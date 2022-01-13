package com.hover.stax.transfers

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.NonTemplateVariableItemBinding

class NonTemplateVariableAdapter(private val variables: List<String>, private val editTextListener: NonTemplateVariableInputListener) : RecyclerView.Adapter<NonTemplateVariableAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: NonTemplateVariableItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bindItems(variableKey: String) {
            val inputTextWatcher: TextWatcher = object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    editTextListener.nonTemplateVariableInputUpdated (variableKey,  charSequence.toString().replace(",".toRegex(), ""))
                }
            }

            binding.variableInput.setHint(variableKey)
            binding.variableInput.addTextChangedListener(inputTextWatcher)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NonTemplateVariableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(variables[position])
    }

    interface NonTemplateVariableInputListener {
        fun nonTemplateVariableInputUpdated(key:String, value: String)
    }

    override fun getItemCount(): Int {
     return variables.size
    }
}