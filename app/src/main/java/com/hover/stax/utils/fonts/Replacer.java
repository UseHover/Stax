package com.hover.stax.utils.fonts;

public interface Replacer {
    void setDefaultFont(String defaultFontAsset);
    void setBoldFont(String boldFontAsset);
    void setItalicFont(String italicFontAsset);
    void setBoldItalicFont(String boldItalicFontAsset);
    void setLightFont(String lightFontAsset);
    void setCondensedFont(String condensedFontAsset);
    void setThinFont(String thinFontAsset);
    void setMediumFont(String mediumFontAsset);

    void applyFont();
}
