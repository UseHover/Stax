package com.hover.stax.database;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;

public class Converters {

    @TypeConverter
    public String fromArray(ArrayList<String> strings) {
        if (strings == null) return null;
        StringBuilder string = new StringBuilder();
        for (String s : strings) string.append(s).append(",");
        return string.toString();
    }

    @TypeConverter
    public ArrayList<String> toArray(String concatenatedStrings) {
        return concatenatedStrings != null ? new ArrayList<>(Arrays.asList(concatenatedStrings.split(","))) : new ArrayList<>();
    }
}
