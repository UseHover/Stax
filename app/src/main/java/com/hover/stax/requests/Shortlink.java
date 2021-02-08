package com.hover.stax.requests;

import java.util.HashMap;
import java.util.Map;

public class Shortlink {
	private String link;
	private Map<String, String> linksMap = new HashMap();

	public Shortlink(String link) {
		this.link = link;
		linksMap.put("houseofmayake", "F34C6jpP2JB3bqD3rvd8P0Fi8rmmvB(3o3dcasn/94CA07NXEiP3N90GmvQx0jgj");
	}

	public String expand() {
		return linksMap.get(link.toLowerCase()) !=null ? linksMap.get(link.toLowerCase()) : link;
	}

}
