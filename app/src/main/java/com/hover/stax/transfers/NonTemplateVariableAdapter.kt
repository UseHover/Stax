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
import com.hover.stax.databinding.NonTemplateVariableItemBinding
import com.hover.stax.views.AbstractStatefulInput

class NonTemplateVariableAdapter(private var variables: List<NonTemplateVariable>, private val editTextListener: NonTemplateVariableInputListener) :
        ListAdapter<NonTemplateVariable, NonTemplateVariableAdapter.ViewHolder>(NonTemplateDiffCallback()) {

    @SuppressLint("NotifyDataSetChanged")
    fun updateStates(variables: List<NonTemplateVariable>) {
        this.variables = variables
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: NonTemplateVariableItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bindItems(nonTemplateVariable: NonTemplateVariable) {

            val inputTextWatcher: TextWatcher = object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    nonTemplateVariable.value = charSequence.toString().replace(",".toRegex(), "")
                    editTextListener.nonTemplateVariableInputUpdated (nonTemplateVariable)
                }
            }

            binding.variableInput.addTextChangedListener(inputTextWatcher)
            binding.variableInput.setHint(nonTemplateVariable.key)
            binding.variableInput.tag = nonTemplateVariable.key
            binding.variableInput.text = nonTemplateVariable.value

            nonTemplateVariable.editTextState?.let {
                val ctx : Context = binding.root.context
                val title = nonTemplateVariable.key

                if(it == AbstractStatefulInput.ERROR) {
                    val message = ctx.getString(R.string.enterValue_non_template_error, title).lowercase()
                    binding.variableInput.setState(message, it)
                }
                else binding.variableInput.setState(null, it)
            }
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
        fun nonTemplateVariableInputUpdated(nonTemplateVariable: NonTemplateVariable)
    }

    override fun getItemCount(): Int {
     return variables.size
    }
}

private class NonTemplateDiffCallback : DiffUtil.ItemCallback<NonTemplateVariable>() {
    override fun areItemsTheSame(oldItem: NonTemplateVariable, newItem: NonTemplateVariable): Boolean {
        return oldItem.key == newItem.key
    }

    override fun areContentsTheSame(oldItem: NonTemplateVariable, newItem: NonTemplateVariable): Boolean {
        return oldItem == newItem
    }

}