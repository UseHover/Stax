package com.hover.stax.utils.paymentLinkCryptography;

class ZRMClass {
	//TODO
	String init(String initial) {
		char[] msg = initial.toCharArray();
		String rep = "";
		for(int i=0; i<msg.length; i++) {
			try{
				if(msg[i] == '0' && msg[i+1] == '0' && msg[i+2] == '0') {
					//msg[i]
				}
			}catch (Exception e) {
				//Index exception
			}
		}
		return "";
	}
}
