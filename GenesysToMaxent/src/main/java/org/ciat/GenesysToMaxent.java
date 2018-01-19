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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class GenesysToMaxent {

	// index of columns
	private Map<String, Integer> colIndex = new LinkedHashMap<String, Integer>();
	// target columns
	private String[] colTarget = { "taxonkey", "decimallongitude", "decimallatitude", "countrycode" };
	private String[] colMaxent = { "species", "lon", "lat", "country", "type" };

	private static final String SEPARATOR = "\t";

	public static void main(String[] args) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date));

		String fileName = "data.csv";
		if (args.length > 0) {
			fileName = args[0];
		} else {
			System.out.println("File not provided in arguments, using " + fileName + " as default");
		}

		GenesysToMaxent app = new GenesysToMaxent();
		app.extract(fileName);

		date = new Date();
		System.out.println();
		System.out.println(dateFormat.format(date));
	}

	private void extract(String fileName) {

		File input = new File(fileName);
		File outputDir = new File("coords");


		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();
			if (colIndex.isEmpty()) {
				colIndex = getColumnsIndex(line);
			}
			String header = "";
			for (String col : colMaxent) {
				header += col + SEPARATOR;
			}
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

			Map<String, PrintWriter> writers = new TreeMap<String, PrintWriter>();
			Map<String, Set<String>> coords = new TreeMap<String, Set<String>>();

			line = reader.readLine();
			while (line != null) {
				line += SEPARATOR + " ";

				String[] values = line.split(SEPARATOR);

				String taxon = values[colIndex.get("taxonkey")];
				File output = new File(outputDir.getName() + "//" + taxon + ".csv");

				if (!writers.keySet().contains(taxon)) {
					writers.put(taxon, new PrintWriter(new BufferedWriter(new FileWriter(output, true))));
					// writers.get(taxon).println(header);
					coords.put(taxon, new TreeSet<String>());
				}

				// get only target values to print
				String coord = getTargetValues(values);
				// include them only if they are new to avoid duplicates
				if (!coords.get(taxon).contains(coord)) {
					writers.get(taxon).println(coord);
					coords.get(taxon).add(coord);
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

			for (String key : writers.keySet()) {
				writers.get(key).flush();
				writers.get(key).close();
			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + input.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	/** Getting only targeted values **/
	private String getTargetValues(String[] values) {
		String output = "";
		for (String col : colTarget) {
			if (colIndex.get(col) != null) {
				output += values[colIndex.get(col)];
			} else {
				System.out.println("\"" + col + "\" is a target column not found in the file");
			}
			output += SEPARATOR;
		}
		output += Basis.G;
		return output;
	}

	private Map<String, Integer> getColumnsIndex(String line) {
		Map<String, Integer> colIndex = new LinkedHashMap<String, Integer>();
		String[] columnNames = line.split(SEPARATOR);
		for (int i = 0; i < columnNames.length; i++) {
			colIndex.put(columnNames[i], i);
		}
		return colIndex;
	}

	private enum Basis {
		G, H
	}


}
