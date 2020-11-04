package com.hover.stax.requestAccount;

import java.util.ArrayList;
import java.util.List;

class AccountsByCountry {
	String country, serviceName;

	public AccountsByCountry(String country, String serviceName) {
		this.country = country;
		this.serviceName = serviceName;
	}

	public String getCountry() {
		return country;
	}

	public String getServiceName() {
		return serviceName;
	}



	List<AccountsByCountry> init() {
		List<AccountsByCountry> accounts = new ArrayList<>();
		accounts.add(new AccountsByCountry("Cameroon", "MTN"));
		accounts.add(new AccountsByCountry("Ethiopia", "MTN"));
		accounts.add(new AccountsByCountry("Ghana", "MTN"));
		accounts.add(new AccountsByCountry("Kenya", "MTN"));
		accounts.add(new AccountsByCountry("Nigeria", "GTbank"));
		accounts.add(new AccountsByCountry("Indonesia", "MTN"));
		accounts.add(new AccountsByCountry("Sierra Leon", "MTN"));
		accounts.add(new AccountsByCountry("Senegal", "MTN"));
		accounts.add(new AccountsByCountry("South Africa", "MTN"));
		accounts.add(new AccountsByCountry("Tanzania", "MTN"));
		accounts.add(new AccountsByCountry("Zambia", "MTN"));
		accounts.add(new AccountsByCountry("Zimbabwe", "MTN"));
		return accounts;
	}

	List<String> filterAccounts(List<AccountsByCountry>accounts,  String country) {
		List<String> result = new ArrayList<>();
		for(AccountsByCountry act : accounts) {
			if(act.getCountry().equals(country)) result.add(act.getServiceName());
		}
		return result;
	}
	
	
}
