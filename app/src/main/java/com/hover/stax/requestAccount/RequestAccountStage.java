package com.hover.stax.requestAccount;


enum RequestAccountStage{
	SELECT_COUNTRY, SELECT_NETWORK, ENTER_CONTACT;
	private static RequestAccountStage[] vals = values();

	public RequestAccountStage next() { return vals[(this.ordinal() + 1) % vals.length];}
	public RequestAccountStage prev() {return vals[(this.ordinal() - 1) % vals.length]; }
}
