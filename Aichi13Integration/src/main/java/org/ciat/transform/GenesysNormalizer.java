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

import org.ciat.model.Basis;
import org.ciat.model.DataSourceName;
import org.ciat.model.FileProgressBar;
import org.ciat.model.TaxonFinder;
import org.ciat.model.Utils;

public class GenesysNormalizer extends Normalizer {

	private final String INPUT_SEPARATOR = ",";

	public void process(File input, File output) {

		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output, true)));
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();
			if (colIndex.isEmpty()) {
				line = line.replaceAll(" ", "");
				colIndex = Utils.getColumnsIndex(line, INPUT_SEPARATOR);
			}

			/* progress bar */
			FileProgressBar bar = new FileProgressBar(input.length());
			/* */

			line = reader.readLine();
			while (line != null) {
				line = line.replaceAll("\"", "");

				line += SEPARATOR + " ";

				String[] values = line.split(INPUT_SEPARATOR);

				if (isUseful(values)) {
					String result = normalize(values);
					writer.println(result);
				}
				bar.update(line.length());
				line = reader.readLine();

			}

			bar.finish();

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + input.getAbsolutePath());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private boolean isUseful(String[] values) {
		if (values.length == colIndex.size()) {
			String lon = values[colIndex.get("decimallongitude")];
			String lat = values[colIndex.get("decimallatitude")];
			if (Utils.isNumeric(lon) && Utils.isNumeric(lat)) {
				if (Utils.areValidCoordinates(lat, lon)) {
					return true;
				}
			}
		}
		return false;
	}

	private String normalize(String[] values) {
		String lon = values[colIndex.get("decimallongitude")];
		String lat = values[colIndex.get("decimallatitude")];
		String country = values[colIndex.get("a.orgCty")];
		String basis = Basis.G.toString();
		System.out.println(values[colIndex.get("t.taxonName")]);
		String taxonKey = TaxonFinder.getInstance().fetchTaxonInfo(values[colIndex.get("t.taxonName")]);
		String result = taxonKey + SEPARATOR + lon + SEPARATOR + lat + SEPARATOR + country + SEPARATOR + basis
				+ SEPARATOR + getDataSourceName();
		return result;
	}

	private DataSourceName getDataSourceName() {
		return DataSourceName.GENESYS;
	}

}
