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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TaxaSegregator {

	// index of columns
	private Map<String, Integer> colIndex = new LinkedHashMap<>();
	// target columns
	private String[] colTarget = { "gbifid", "countrycode", "taxonkey", "scientificname", "basisofrecord",
			"decimallatitude", "decimallongitude", "coordinateuncertaintyinmeters", "coordinateprecision", "elevation",
			"elevationaccuracy", "depth", "depthaccuracy" };

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

		TaxaSegregator app = new TaxaSegregator();
		app.extract(fileName);

		date = new Date();
		System.out.println();
		System.out.println(dateFormat.format(date));
	}

	private void extract(String fileName) {

		File input = new File(fileName);
		File outputDir = new File("data");

		clearOutputDirectory(outputDir);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();
			if (colIndex.isEmpty()) {
				colIndex = getColumnsIndex(line);
			}
			String header = "";
			for (String col : colTarget) {
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

			line = reader.readLine();
			while (line != null) {
				line += SEPARATOR + " ";
				String[] values = line.split(SEPARATOR);
				String taxon = values[colIndex.get("taxonkey")];
				File output = new File(outputDir.getName() + "//" + taxon + ".csv");
				if (isUsable(values)) {

					try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output, true)))) {
						if (!output.exists()) {
							writer.println(header);
						}
						writer.println(getTargeValues(values));
					}
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

	private void clearOutputDirectory(File outputDir) {
		if (outputDir.exists()) {
			outputDir.delete();
		} else {
			outputDir.mkdir();
		}
	}

	/** Getting only targeted values **/
	private String getTargeValues(String[] values) {
		String output = "";
		for (String col : colTarget) {
			if (colIndex.get(col) != null) {
				output += values[colIndex.get(col)];
			} else {
				System.out.println("\"" + col + "\" is a target colum not found in the file");
			}
			output += SEPARATOR;
		}
		return output;
	}

	private boolean isUsable(String[] values) {

		if (!values[colIndex.get("taxonrank")].contains("SPECIES")) {
			return false;
		}

		/* excluding issues */
		Set<String> issues = new LinkedHashSet<>();
		issues.add("COORDINATE_OUT_OF_RANGE");
		issues.add("COUNTRY_COORDINATE_MISMATCH");
		issues.add("ZERO_COORDINATE");

		for (String issue : issues) {
			if (values[colIndex.get("issue")].contains(issue)) {
				return false;
			}
		}

		if (values[colIndex.get("decimallatitude")].equals("") || values[colIndex.get("decimallongitude")].equals("")) {
			return false;
		}

		return true;
	}

	private Map<String, Integer> getColumnsIndex(String line) {
		Map<String, Integer> colIndex = new LinkedHashMap<String, Integer>();
		String[] columnNames = line.split(SEPARATOR);
		for (int i = 0; i < columnNames.length; i++) {
			colIndex.put(columnNames[i], i);
		}
		return colIndex;
	}

}
