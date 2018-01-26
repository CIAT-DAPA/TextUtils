package org.ciat.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Utils {
	public static boolean isNumeric(String str) {
		if (str == null) {
			return false;
		}
		try {
			@SuppressWarnings("unused")
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static Map<String, Integer> getColumnsIndex(String line, String separator) {
		Map<String, Integer> colIndex = new LinkedHashMap<String, Integer>();
		String[] columnNames = line.split(separator);
		for (int i = 0; i < columnNames.length; i++) {
			colIndex.put(columnNames[i], i);
		}
		return colIndex;
	}
}