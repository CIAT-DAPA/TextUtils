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
import java.util.Set;

import org.ciat.model.Basis;
import org.ciat.model.DataSourceName;
import org.ciat.model.FileProgressBar;
import org.ciat.model.TargetTaxa;
import org.ciat.model.Utils;

public class GBIFNormalizer extends Normalizer {

	private Set<String> taxonKeys;

	/** @return output file */
	public void process(File input, File output) {

		taxonKeys = TargetTaxa.getInstance().getSpeciesKeys();

		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output, true)));
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();
			colIndex = Utils.getColumnsIndex(line, SEPARATOR);
			/* */

			/* progress bar */
			FileProgressBar bar = new FileProgressBar(input.length());
			/* */

			line = reader.readLine();
			while (line != null) {
				line += SEPARATOR + " ";
				String[] values = line.split(SEPARATOR);
				if (isUseful(values)) {
					String result = normalize(values);
					writer.println(result);
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

	private String normalize(String[] values) {
		String result = values[colIndex.get("taxonkey")] + SEPARATOR + values[colIndex.get("decimallongitude")]
				+ SEPARATOR + values[colIndex.get("decimallatitude")] + SEPARATOR + values[colIndex.get("countrycode")]
				+ SEPARATOR + getBasis(values[colIndex.get("basisofrecord")]) + SEPARATOR + getDataSourceName();
		return result;
	}

	private boolean isUseful(String[] values) {

		// excluding CWR dataset
		if (colIndex.get("datasetkey") != null
				&& values[colIndex.get("datasetkey")].contains("07044577-bd82-4089-9f3a-f4a9d2170b2e")) {
			return false;
		}

		// only allow species and subspecies
		if (colIndex.get("taxonrank") != null) {
			if (!values[colIndex.get("taxonrank")].contains("SPECIES")) {
				return false;
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
		
		if(!Utils.areValidCoordinates(values[colIndex.get("decimallatitude")],values[colIndex.get("decimallongitude")])){
			return false;
		}
		

		if (colIndex.get("taxonkey") != null) {
			/* check if it's a target taxon */
			String taxon = values[colIndex.get("taxonkey")];
			if (!taxonKeys.contains(taxon)) {
				return false;
			}
		}

		return true;
	}

	private Basis getBasis(String basisofrecord) {
		if (basisofrecord.toUpperCase().equals("LIVING_SPECIMEN")) {
			return Basis.G;
		}
		return Basis.H;
	}

	private DataSourceName getDataSourceName() {
		return DataSourceName.GBIF;
	}

}