package org.ciat.export;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.ciat.model.Basis;
import org.ciat.model.FileProgressBar;
import org.ciat.model.Utils;

public class GBIFToMaxent {

	// index of columns
	private Map<String, Integer> colIndex = new LinkedHashMap<String, Integer>();
	// target columns
	private String[] colTarget = { "taxonkey", "decimallongitude", "decimallatitude", "countrycode" };

	private static final String SEPARATOR = "\t";

	/** @return output file */
	public void process(File input) {

		File outputDir = new File("coords");
		if(!outputDir.exists()){
			outputDir.mkdirs();
		}

		clearOutputDirectory(outputDir);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();
			if (colIndex.isEmpty()) {
				colIndex = Utils.getColumnsIndex(line,SEPARATOR);
			}

			/* progress bar */
			FileProgressBar bar = new FileProgressBar(input.length());
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
				bar.update(line.length());
				/* */
				line = reader.readLine();

			}

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

	private void clearOutputDirectory(File outputDir) {
		if (outputDir.exists()) {
			for (File f : outputDir.listFiles()) {
				f.delete();
			}
		} else {
			outputDir.mkdir();
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
		output += getBasis(values[colIndex.get("basisofrecord")]);
		return output;
	}





	private Basis getBasis(String basisofrecord) {
		if (basisofrecord.toUpperCase().equals("LIVING_SPECIMEN")) {
			return Basis.G;
		}
		return Basis.H;
	}

}
