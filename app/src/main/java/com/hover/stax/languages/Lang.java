package com.hover.stax.languages;

import java.util.Locale;

public class Lang {
	public String code, name;

	public Lang(String code) {
		this.code = code;
		Locale l = new Locale(code);
		name = l.getDisplayLanguage(l);
	}

	@Override
	public String toString() { return name; }

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Lang)) return false;
		Lang l = (Lang) other;
		return code.equals(l.code); }
}
