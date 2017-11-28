package org.ciat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class CWRFilterer {

	private Map<String, Integer> colIndex;
	private Map<Integer, CWR> taxaCWR;
	private static final String SEPARATOR = "\t";

	public static void main(String[] args) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date));

		String fileName = "gbif.csv";
		if (args.length > 0) {
			fileName = args[0];
		} else {
			System.out.println("File not provided in arguments, using " + fileName + " as default");
		}

		CWRFilterer app = new CWRFilterer();
		app.extract(fileName);

		date = new Date();
		System.out.println();
		System.out.println(dateFormat.format(date));
	}

	private void extract(String fileName) {

		File input = new File(fileName);
		File output = new File("data.csv");

		File taxaFile = new File("cwr.csv");
		taxaCWR = loadTargetCRW(taxaFile);

		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)));
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();
			colIndex = getColumnsIndex(line);
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
				String[] values = line.split(SEPARATOR);
				if (isUseful(values)) {
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

	private boolean isUseful(String[] values) {

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

			if (!isNumeric(values[colIndex.get("decimallatitude")])) {
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
			if (!isNumeric(values[colIndex.get("decimallongitude")])) {
				return false;
			} else {
				Double lat = Double.parseDouble(values[colIndex.get("decimallongitude")]);
				if (lat == 0 || lat > 180 || lat < -180) {
					return false;
				}
			}
		}


		/* check if it's a target taxon */
		if (colIndex.get("taxonkey") != null && colIndex.get("countrycode") != null) {
			String country = values[colIndex.get("countrycode")];
			Integer taxonKey = Integer.parseInt(values[colIndex.get("taxonkey")]);
			if (taxaCWR.containsKey(taxonKey)) {
				/* check if taxon is native in that country */
				if (taxaCWR.get(taxonKey).getNativeCountries().contains(country)) {
					return true;
				}
			}
		}

		return false;
	}

	private Map<Integer, CWR> loadTargetCRW(File vocabularyFile) {
		Map<Integer, CWR> CWRs = new TreeMap<Integer, CWR>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(vocabularyFile)))) {

			String line = reader.readLine(); // skip header
			line = reader.readLine();
			while (line != null) {
				if (!line.isEmpty()) {
					String[] values = line.split(SEPARATOR);
					Integer taxonKey = Integer.parseInt(values[0]);
					String country = values[1];
					if (CWRs.containsKey(taxonKey)) {
						CWRs.get(taxonKey).getNativeCountries().add(country);
					} else {
						CWR newCWR = new CWR(taxonKey);
						newCWR.getNativeCountries().add(country);
						CWRs.put(taxonKey, newCWR);
					}

				}
				line = reader.readLine();
			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + vocabularyFile.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("Cannot read " + vocabularyFile.getAbsolutePath());
		}

		return CWRs;
	}

	private Map<String, Integer> getColumnsIndex(String line) {
		Map<String, Integer> colIndex = new LinkedHashMap<String, Integer>();
		String[] columnNames = line.split(SEPARATOR);
		for (int i = 0; i < columnNames.length; i++) {
			colIndex.put(columnNames[i].trim(), i);
		}
		return colIndex;
	}
	
	public static boolean isNumeric(String str) {
		try {
			@SuppressWarnings("unused")
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

}
