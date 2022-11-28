package com.hover.stax.transfers

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.R
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.databinding.CountryItemBinding
import com.hover.stax.databinding.InputItemBinding
import com.hover.stax.utils.splitCamelCase
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxTextInput

class NonStandardVariableAdapter(private var variables: LinkedHashMap<String, String>, private val editTextListener: NonStandardVariableInputListener, context: Context) :
    ArrayAdapter<HashMap<String, String>>(context, 0) {

    override fun getView(position: Int, v: View?, parent: ViewGroup): View {
        return initView(position, v, parent)
    }

    private fun initView(position: Int, v: View?, parent: ViewGroup): View {
        val holder: NonStandardVariableAdapter.ViewHolder
        var view = v
        if (view == null) {
            val binding = InputItemBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root

            holder = ViewHolder(binding)
            holder.setVar(variables.keys.toList()[position], variables.values.toList()[position])
            holder.setState(position)
        }

        return view
    }

//    override fun getItem(position: Int): HashMap<String, String>? = if (count > 0) HashMap(variables.keys.toList()[position], variables.values.toList()[position]) else null

    override fun getCount(): Int = variables.size

    inner class ViewHolder(val binding: InputItemBinding) {
        private val input = binding.variableInput

        fun setVar(key: String, v: String) {
            val inputTextWatcher: TextWatcher = object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence,i: Int,i1: Int,i2: Int) {}
                override fun afterTextChanged(editable: Editable) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    editTextListener.nonStandardVarUpdate(key, charSequence.toString())
                }
            }

            input.addTextChangedListener(inputTextWatcher)
            input.setHint(key.splitCamelCase())
            input.tag = key
            input.setText(v)
        }

        fun setState(pos: Int) {
            if (variables.values.toList()[pos].isNullOrEmpty())
                input.setState(context.getString(R.string.enterValue_non_template_error, variables.keys.toList()[pos].lowercase()), AbstractStatefulInput.ERROR)
            else input.setState(null, AbstractStatefulInput.SUCCESS)
        }
    }

    fun validates(): Boolean {
        if (variables.isNullOrEmpty()) return true
        for ((k, v) in variables)
            if (v.isNullOrEmpty()) return false
        return true
    }

    interface NonStandardVariableInputListener {
        fun nonStandardVarUpdate(key: String, value: String)
    }
}