package org.ciat.transform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ciat.model.Basis;

public class Normalizer implements Normalizable {

	// output separator
	public static final String SEPARATOR = "\t";
	public static final int YEAR = 1950;
	// index of columns
	protected Map<String, Integer> colIndex = new LinkedHashMap<String, Integer>();
	// target columns
	private String[] colTarget = { "taxonkey", "decimallongitude", "decimallatitude", "countrycode", "basis",
			"source" };

	/** Getting only targeted values **/
	public String getTargetValues(String[] values) {
		String output = "";
		for (String col : colTarget) {
			if (colIndex.get(col) != null) {
				output += values[colIndex.get(col)];
				output += SEPARATOR;
			} else {
				System.out.println("\"" + col + "\" is a target column not found in the file");
			}
		}
		return output;
	}

	public Basis getBasis(String basisofrecord) {
		if (basisofrecord.toUpperCase().equals("LIVING_SPECIMEN")) {
			return Basis.G;
		}
		return Basis.H;
	}

	public Set<String> loadTargetTaxa(File vocabularyFile) {
		Set<String> filters = new TreeSet<String>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(vocabularyFile)))) {

			String line = reader.readLine();
			while (line != null) {
				if (!line.isEmpty()) {
					filters.add(line);
				}
				line = reader.readLine();
			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + vocabularyFile.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("Cannot read " + vocabularyFile.getAbsolutePath());
		}
		return filters;
	}

	public String getHeader() {
		String result = "";
		for (String field : colTarget) {
			result += field + SEPARATOR;
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}
}
