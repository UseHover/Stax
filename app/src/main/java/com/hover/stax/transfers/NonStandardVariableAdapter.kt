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

class NonStandardVariableAdapter(private var variables: List<NonStandardVariable>, private val editTextListener: NonStandardVariableInputListener) :
        ListAdapter<NonStandardVariable, NonStandardVariableAdapter.ViewHolder>(NonStandardDiffCallback()) {

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(variables: List<NonStandardVariable>) {
        this.variables = variables
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: NonStandardVariableItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bindItems(nonStandardVariable: NonStandardVariable) {

            val inputTextWatcher: TextWatcher = object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    nonStandardVariable.value = charSequence.toString().replace(",".toRegex(), "")
                    editTextListener.nonStandardVariableInputUpdated (nonStandardVariable)
                }
            }

            binding.variableInput.addTextChangedListener(inputTextWatcher)
            binding.variableInput.setHint(nonStandardVariable.key)
            binding.variableInput.tag = nonStandardVariable.key
            binding.variableInput.text = nonStandardVariable.value

            nonStandardVariable.editTextState?.let {
                val ctx : Context = binding.root.context
                val title = nonStandardVariable.key

                if(it == AbstractStatefulInput.ERROR) {
                    val message = ctx.getString(R.string.enterValue_non_template_error, title).lowercase()
                    binding.variableInput.setState(message, it)
                }
                else binding.variableInput.setState(null, it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NonStandardVariableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(variables[position])
    }

    interface NonStandardVariableInputListener {
        fun nonStandardVariableInputUpdated(nonStandardVariable: NonStandardVariable)
    }

    override fun getItemCount(): Int {
     return variables.size
    }
}

private class NonStandardDiffCallback : DiffUtil.ItemCallback<NonStandardVariable>() {
    override fun areItemsTheSame(oldItem: NonStandardVariable, newItem: NonStandardVariable): Boolean {
        return oldItem.key == newItem.key
    }

    override fun areContentsTheSame(oldItem: NonStandardVariable, newItem: NonStandardVariable): Boolean {
        return oldItem == newItem
    }

}