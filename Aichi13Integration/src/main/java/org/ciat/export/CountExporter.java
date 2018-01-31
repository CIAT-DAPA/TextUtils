package org.ciat.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.ciat.App;
import org.ciat.model.Utils;
import org.ciat.transform.Normalizer;

public class CountExporter {

	public void process() {
		for (String key : App.totalRecords.keySet()) {
			int totalRecords = App.totalRecords.get(key);
			int totalUseful = 0;
			if (App.totalUseful.containsKey(key)) {
				totalUseful = App.totalUseful.get(key);
			}
			int totalGRecords = 0;
			if (App.totalGRecords.containsKey(key)) {
				totalGRecords = App.totalGRecords.get(key);
			}
			int totalGUseful = 0;
			if (App.totalGUseful.containsKey(key)) {
				totalGUseful = App.totalGUseful.get(key);
			}
			int totalHRecords = 0;
			if (App.totalHRecords.containsKey(key)) {
				totalHRecords = App.totalHRecords.get(key);
			}
			int totalHUseful = 0;
			if (App.totalHUseful.containsKey(key)) {
				totalHUseful = App.totalHUseful.get(key);
			}
			int totalPost1950 = 0;
			if (App.totalPost1950.containsKey(key)) {
				totalPost1950 = App.totalPost1950.get(key);
			}
			int totalPre1950 = 0;
			if (App.totalPre1950.containsKey(key)) {
				totalPre1950 = App.totalPre1950.get(key);
			}

			File outputDir = new File("gap_analysis" + "/" + key + "/");
			if (!outputDir.exists()) {
				outputDir.mkdirs();
			} else {
				Utils.clearOutputDirectory(outputDir);
			}

			File output = new File(outputDir.getAbsolutePath() + "/counts.csv");

			try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
				writer.println("totalRecords" + Normalizer.SEPARATOR + "totalUseful" + Normalizer.SEPARATOR
						+ "totalGRecords" + Normalizer.SEPARATOR + "totalGUseful" + Normalizer.SEPARATOR
						+ "totalHRecords" + Normalizer.SEPARATOR + "totalHUseful" + Normalizer.SEPARATOR
						+ "totalPost1950" + Normalizer.SEPARATOR + "totalPre1950");
				writer.println(totalRecords + Normalizer.SEPARATOR + totalUseful + Normalizer.SEPARATOR + totalGRecords
						+ Normalizer.SEPARATOR + totalGUseful + Normalizer.SEPARATOR + totalHRecords
						+ Normalizer.SEPARATOR + totalHUseful + Normalizer.SEPARATOR + totalPost1950
						+ Normalizer.SEPARATOR + totalPre1950);

			} catch (FileNotFoundException e) {
				System.out.println("File not found " + output.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
