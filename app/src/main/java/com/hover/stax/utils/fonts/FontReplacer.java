package com.hover.stax.utils.fonts;

import android.content.Context;
import android.graphics.Typeface;

public class FontReplacer {
    static Typeface defaultFont;
    static Typeface boldFont;
    static Typeface italicFont;
    static Typeface boldItalicFont;
    static Typeface lightFont;
    static Typeface condensedFont;
    static Typeface thinFont;
    static Typeface mediumFont;

    public static Replacer Build(Context context){
        return new ReplacerImpl(context);
    }

    public static Typeface getDefaultFont() {
        return defaultFont;
    }

    public static Typeface getBoldFont() {
        return boldFont;
    }

    public static Typeface getItalicFont() {
        return italicFont;
    }

    public static Typeface getBoldItalicFont() {
        return boldItalicFont;
    }

    public static Typeface getLightFont() {
        return lightFont;
    }

    public static Typeface getCondensedFont() {
        return condensedFont;
    }

    public static Typeface getThinFont() {
        return thinFont;
    }

    public static Typeface getMediumFont() {
        return mediumFont;
    }
}
