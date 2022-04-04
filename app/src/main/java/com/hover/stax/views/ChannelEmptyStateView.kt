package com.hover.stax.views

import android.app.Activity
import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.text.HtmlCompat
import com.hover.stax.R
import com.hover.stax.databinding.NoAccountEmptyStateBinding

class ChannelEmptyStateView (context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
val binding: NoAccountEmptyStateBinding = NoAccountEmptyStateBinding.inflate(LayoutInflater.from(context), this, true)

	fun setup(activity: Activity) {
		binding.root.visibility = GONE
		binding.informUs.setOnClickListener {
			RequestServiceDialog(activity).showIt()
		}
	}

	fun show(searchValue: String) {
		val content = resources.getString(R.string.no_accounts_found_desc,  searchValue)
		binding.noAccountFoundDesc.apply {
			text = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY)
			movementMethod = LinkMovementMethod.getInstance()
		}

		binding.root.visibility = VISIBLE
	}

	fun dismiss() {
		binding.root.visibility = GONE
	}

}