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
import java.util.Set;
import java.util.TreeMap;

import org.ciat.model.Basis;
import org.ciat.model.FileProgressBar;
import org.ciat.model.Utils;

public class GenesysNormalizer extends Normalizer {

	public void process(File input, File output) {

		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output, true)));
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();
			if (colIndex.isEmpty()) {
				colIndex = Utils.getColumnsIndex(line, SEPARATOR);
			}

			/* progress bar */
			FileProgressBar bar = new FileProgressBar(input.length());
			/* */

			Map<String, Set<String>> coords = new TreeMap<String, Set<String>>();

			line = reader.readLine();
			while (line != null) {
				line += SEPARATOR + " ";

				String[] values = line.split(SEPARATOR);

				String taxon = values[colIndex.get("taxonkey")];

				// get only target values to print
				String occurrence = getTargetValues(values);
				// include them only if they are new to avoid duplicates
				if (!coords.get(taxon).contains(occurrence)) {
					writer.println(occurrence);
					coords.get(taxon).add(occurrence);
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
	
	
	public Basis getBasis(String basisofrecord) {
		return Basis.G;
	}



}
