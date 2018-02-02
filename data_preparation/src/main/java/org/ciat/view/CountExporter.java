package org.ciat.view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.ciat.control.Normalizer;
import org.ciat.model.Basis;
import org.ciat.model.MapCounter;
import org.ciat.model.TargetTaxa;
import org.ciat.model.TaxonFinder;
import org.ciat.model.Utils;

public class CountExporter {

	public static MapCounter totalRecords = new MapCounter();
	public static MapCounter totalUseful = new MapCounter();
	public static MapCounter totalGRecords = new MapCounter();
	public static MapCounter totalGUseful = new MapCounter();
	public static MapCounter totalHRecords = new MapCounter();
	public static MapCounter totalHUseful = new MapCounter();
	public static MapCounter totalPost1950 = new MapCounter();
	public static MapCounter totalPre1950 = new MapCounter();

	public void process() {
		exportSpeciesCounters();
		exportDatasetCounter();
	}

	private void exportDatasetCounter() {
		File output = new File(Executer.prop.getProperty("file.taxonfinder.summary"));
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
			writer.println("species.matched" + Normalizer.SEPARATOR + "species.unmatched");
			writer.println(TaxonFinder.getInstance().getMatchedTaxa().keySet().size() + Normalizer.SEPARATOR
					+ TaxonFinder.getInstance().getUnmatchedTaxa().size());

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + output.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void exportSpeciesCounters() {

		// header of summary file
		File outputSummary = new File(Executer.prop.getProperty("file.counts.summary"));
		String header = "totalRecords" + Normalizer.SEPARATOR + "totalUseful" + Normalizer.SEPARATOR + "totalGRecords"
				+ Normalizer.SEPARATOR + "totalGUseful" + Normalizer.SEPARATOR + "totalHRecords" + Normalizer.SEPARATOR
				+ "totalHUseful" + Normalizer.SEPARATOR + "totalPost1950" + Normalizer.SEPARATOR + "totalPre1950";
		try (PrintWriter writerSummary = new PrintWriter(new BufferedWriter(new FileWriter(outputSummary, true)))) {
			writerSummary.println("taxonkey"+Normalizer.SEPARATOR+header);

			// for each target taxon in the list
			for (String key : TargetTaxa.getInstance().getSpeciesKeys()) {
				int countTotalRecords = 0;
				if (totalRecords.containsKey(key)) {
					countTotalRecords = totalRecords.get(key);
				}
				int countTotalUseful = 0;
				if (totalUseful.containsKey(key)) {
					countTotalUseful = totalUseful.get(key);
				}
				int countTotalGRecords = 0;
				if (totalGRecords.containsKey(key)) {
					countTotalGRecords = totalGRecords.get(key);
				}
				int countTotalGUseful = 0;
				if (totalGUseful.containsKey(key)) {
					countTotalGUseful = totalGUseful.get(key);
				}
				int countTotalHRecords = 0;
				if (totalHRecords.containsKey(key)) {
					countTotalHRecords = totalHRecords.get(key);
				}
				int countTotalHUseful = 0;
				if (totalHUseful.containsKey(key)) {
					countTotalHUseful = totalHUseful.get(key);
				}
				int countTotalPost1950 = 0;
				if (totalPost1950.containsKey(key)) {
					countTotalPost1950 = totalPost1950.get(key);
				}
				int countTotalPre1950 = 0;
				if (totalPre1950.containsKey(key)) {
					countTotalPre1950 = totalPre1950.get(key);
				}

				File outputDir = new File(Executer.prop.getProperty("path.counts") + "/" + key + "/");
				if (!outputDir.exists()) {
					outputDir.mkdirs();
				} else {
					Utils.clearOutputDirectory(outputDir);
				}

				File output = new File(outputDir.getAbsolutePath() + "/counts.csv");
				String countsLine = countTotalRecords + Normalizer.SEPARATOR + countTotalUseful
						+ Normalizer.SEPARATOR + countTotalGRecords + Normalizer.SEPARATOR + countTotalGUseful
						+ Normalizer.SEPARATOR + countTotalHRecords + Normalizer.SEPARATOR + countTotalHUseful
						+ Normalizer.SEPARATOR + countTotalPost1950 + Normalizer.SEPARATOR + countTotalPre1950;
				

				try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
					
					writer.println(header);
					writer.println(countsLine);
					writerSummary.println(key+Normalizer.SEPARATOR +countsLine);

				} catch (FileNotFoundException e) {
					System.out.println("File not found " + output.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found " + outputSummary.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void updateCounters(String taxonkey, boolean useful, String year, Basis basis) {
		totalRecords.increase(taxonkey);

		if (Utils.isNumeric(year)) {
			Integer yearNumber = Integer.parseInt(year);
			if (yearNumber >= Normalizer.YEAR) {
				totalPost1950.increase(taxonkey);
			} else {
				totalPre1950.increase(taxonkey);
			}
		} else {
			totalPre1950.increase(taxonkey);
		}

		if (basis.equals(Basis.G)) {
			totalGRecords.increase(taxonkey);
		} else {
			totalHRecords.increase(taxonkey);
		}

		if (useful) {

			totalUseful.increase(taxonkey);
			if (basis.equals(Basis.G)) {
				totalGUseful.increase(taxonkey);
			} else {
				totalHUseful.increase(taxonkey);
			}
		}

	}

}
