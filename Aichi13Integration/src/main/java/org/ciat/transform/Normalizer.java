package org.ciat.transform;

import java.util.LinkedHashMap;
import java.util.Map;

public class Normalizer implements Normalizable {

	// output separator
	public static final String SEPARATOR = "\t";
	public static final int YEAR = 1950;
	// index of columns
	protected Map<String, Integer> colIndex = new LinkedHashMap<String, Integer>();
	// target columns
	private String[] colTarget = { "taxonkey", "decimallongitude", "decimallatitude", "countrycode", "basis",
			"source" };



	public String getHeader() {
		String result = "";
		for (String field : colTarget) {
			result += field + SEPARATOR;
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}
}
