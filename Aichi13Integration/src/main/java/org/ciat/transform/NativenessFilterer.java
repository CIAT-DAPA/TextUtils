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
import java.util.Map;
import java.util.TreeMap;

import org.ciat.model.FileProgressBar;
import org.ciat.model.TaxonNativeness;
import org.ciat.model.Utils;

public class NativenessFilterer {

	private Map<String, Integer> colIndex;
	private Map<Integer, TaxonNativeness> taxaCWR;
	private static final String SEPARATOR = "\t";

	public void process(File input,File output) {

		File taxaFile = new File("nativeness.csv");
		taxaCWR = loadTargetTaxaNativeness(taxaFile);

		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)));
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();
			colIndex = Utils.getColumnsIndex(line,SEPARATOR);
			writer.println(line);
			/* */

			/* progress bar */
			FileProgressBar bar = new FileProgressBar(input.length());
			/* */

			line = reader.readLine();
			while (line != null) {
				line += SEPARATOR + " ";
				String[] values = line.split(SEPARATOR);
				if (isUseful(values)) {
					writer.println(line);
				}

				/* show progress */
				bar.update(line.length());
				/* */

				line = reader.readLine();

			}
			bar.finish();

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

	private Map<Integer, TaxonNativeness> loadTargetTaxaNativeness(File vocabularyFile) {
		Map<Integer, TaxonNativeness> CWRs = new TreeMap<Integer, TaxonNativeness>();
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
						TaxonNativeness newCWR = new TaxonNativeness(taxonKey);
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



}
