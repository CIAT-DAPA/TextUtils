package org.ciat.transform;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ciat.model.ProgressBar;
import org.ciat.model.Utils;

public class LineFilterer {

	private Map<String, Integer> colIndex;
	private Set<String> taxonKeys;
	private static final String SEPARATOR = "\t";

	/** @return output file */
	public void process(File input,File output) {



		File taxaFile = new File("taxa.csv");
		taxonKeys = loadTargetTaxa(taxaFile);

		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)));
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();
			colIndex = Utils.getColumnsIndex(line,SEPARATOR);
			writer.println(line);
			/* */

			/* progress bar */
			ProgressBar bar = new ProgressBar();
			int exp = (int) Math.ceil((input.length() + "").length()) + 1;
			int dimensionality = (int) Math.pow(2, exp);
			int total = Math.toIntExact(input.length() / dimensionality);
			long done = line.length();
			int lineNumber = 0;
			System.out.println("Reading " + input.length() / 1024 + "KB");
			System.out.println("Updating progress each " + dimensionality + "KB read");
			/* */

			line = reader.readLine();
			while (line != null) {
				line += SEPARATOR + " ";
				if (isUseful(line)) {
					writer.println(line);
				}

				/* show progress */
				done += line.length();
				if (++lineNumber % dimensionality == 0) {
					bar.update(Math.toIntExact(done / dimensionality), total);
				}
				/* */

				line = reader.readLine();

			}
			bar.update(Math.toIntExact(done / dimensionality), total);

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + input.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isUseful(String line) {
		String[] values = line.split(SEPARATOR);

		if (colIndex.get("taxonrank") != null) {
			if (!values[colIndex.get("taxonrank")].contains("SPECIES")) {
				return false;
			}
		}

		/* excluding records with geospatial issues */
		if (colIndex.get("decimallatitude") != null) {
			if ((values[colIndex.get("decimallatitude")].equals("") || values[colIndex.get("decimallatitude")] == null
					|| values[colIndex.get("decimallatitude")].equals("\\N"))
					|| values[colIndex.get("decimallatitude")].equals("null")
					|| values[colIndex.get("decimallatitude")].isEmpty()) {
				return false;
			}

			if (!Utils.isNumeric(values[colIndex.get("decimallatitude")])) {
				return false;
			} else {
				Double lat = Double.parseDouble(values[colIndex.get("decimallatitude")]);
				if (lat == 0 || lat > 90 || lat < -90) {
					return false;
				}
			}
		}
		/* excluding records with geospatial issues */
		if (colIndex.get("decimallongitude") != null) {
			if ((values[colIndex.get("decimallongitude")].equals("") || values[colIndex.get("decimallongitude")] == null
					|| values[colIndex.get("decimallongitude")].equals("\\N"))
					|| values[colIndex.get("decimallongitude")].equals("null")
					|| values[colIndex.get("decimallongitude")].isEmpty()) {
				return false;
			}
			if (!Utils.isNumeric(values[colIndex.get("decimallongitude")])) {
				return false;
			} else {
				Double lat = Double.parseDouble(values[colIndex.get("decimallongitude")]);
				if (lat == 0 || lat > 180 || lat < -180) {
					return false;
				}
			}
		}

		Set<String> issues = new LinkedHashSet<>();
		issues.add("COORDINATE_OUT_OF_RANGE");
		issues.add("COUNTRY_COORDINATE_MISMATCH");
		issues.add("ZERO_COORDINATE");
		for (String issue : issues) {
			if (colIndex.get("issue") != null && values[colIndex.get("issue")].contains(issue)) {
				return false;
			}
		}
		/**/

		if (colIndex.get("taxonkey") != null) {
			/* check if it's a target taxon */
			String taxon = values[colIndex.get("taxonkey")];
			if (!taxonKeys.contains(taxon)) {
				return false;
			}
		}

		return true;
	}

	private Set<String> loadTargetTaxa(File vocabularyFile) {
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


}
