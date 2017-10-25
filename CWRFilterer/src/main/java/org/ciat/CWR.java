package org.ciat;

import java.util.Set;
import java.util.TreeSet;

public class CWR {
	private int taxonKey;
	private Set<String> nativeCountries;
	
	public CWR(int taxonKey) {
		super();
		this.taxonKey = taxonKey;
		this.nativeCountries = new TreeSet<String>();
	}
	
	public int getTaxonKey() {
		return taxonKey;
	}

	public void setTaxonKey(int taxonKey) {
		this.taxonKey = taxonKey;
	}

	public Set<String> getNativeCountries() {
		return nativeCountries;
	}

	public void setNativeCountries(Set<String> nativeCountries) {
		this.nativeCountries = nativeCountries;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof CWR))
			return false;
		CWR castedObj = (CWR) obj;
		if (castedObj.taxonKey == this.taxonKey) {
			return true;
		} else {
			return false;
		}
		
	}
	

}
