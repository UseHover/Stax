package com.hover.stax.transfers

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.R

import com.hover.stax.databinding.InputItemBinding
import com.hover.stax.views.AbstractStatefulInput

class NonStandardVariableAdapter(private var variables: LinkedHashMap<String, String>, private val editTextListener: NonStandardVariableInputListener, private val recyclerView: RecyclerView) :
    RecyclerView.Adapter<NonStandardVariableAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: InputItemBinding): RecyclerView.ViewHolder(binding.root) {
        val input = binding.variableInput

        fun bindItems(key: String, value: String) {

            val inputTextWatcher: TextWatcher = object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    editTextListener.nonStandardVarUpdate(key, charSequence.toString())
                }
            }

            input.addTextChangedListener(inputTextWatcher)
            input.setHint(key)
            input.tag = key
            input.setText(value)
        }
    }

    fun validates(): Boolean {
        var valid = true
        variables.onEachIndexed { index, entry ->
            if (entry.value.isBlank()) {
                valid = false
                (recyclerView.findViewHolderForAdapterPosition(index) as NonStandardVariableAdapter.ViewHolder).input.setState(
                    recyclerView.context.getString(R.string.enterValue_non_template_error, entry.key.lowercase()), AbstractStatefulInput.ERROR
                )
            } else {
                (recyclerView.findViewHolderForAdapterPosition(index) as NonStandardVariableAdapter.ViewHolder).input.setState(
                    null, AbstractStatefulInput.SUCCESS
                )
            }
        }
        return valid
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = InputItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(variables.keys.toList()[position], variables.values.toList()[position])
    }

    interface NonStandardVariableInputListener {
        fun nonStandardVarUpdate(key: String, value: String)
    }

    override fun getItemCount(): Int { return variables.size }
}