package com.hover.stax.utils

import android.text.TextUtils
import java.util.*

fun String.splitCamelCase() : String {
	return StringUtils.splitCamelCase (this)
}

fun String.toHni() : String {
	return this.replace("[", "").replace("]", "").replace("\"", "")
}

fun String.toFilteringStandard() : String {
	return this.lowercase().replace(" ", "");
}

fun String.isAbsolutelyEmpty() : Boolean {
	return TextUtils.isEmpty(this.replace(" ", ""))
}

private object StringUtils {
	fun splitCamelCase(s: String): String {
		val camelCased : String = s.replace(String.format("%s|%s|%s",
			"(?<=[A-Z])(?=[A-Z][a-z])",
			"(?<=[^A-Z])(?=[A-Z])",
			"(?<=[A-Za-z])(?=[^A-Za-z])").toRegex(), " ")
		return capitalize(camelCased)
	}

	private fun capitalize(str: String): String {
		return if (str.isEmpty()) { str }
		else str.substring(0, 1).uppercase(Locale.ROOT) + str.substring(1).lowercase(Locale.ROOT)
	}

}