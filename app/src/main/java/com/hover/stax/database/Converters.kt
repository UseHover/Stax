package com.hover.stax.database

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromArray(strings: ArrayList<String>?): String? {
        if (strings == null) return null
        val string = StringBuilder()
        for (s in strings) string.append(s).append(",")
        return string.toString()
    }

    @TypeConverter
    fun toArray(concatenatedStrings: String?): ArrayList<String> {
        return if (concatenatedStrings != null) ArrayList(listOf(*concatenatedStrings.split(",").toTypedArray())) else ArrayList()
    }
}