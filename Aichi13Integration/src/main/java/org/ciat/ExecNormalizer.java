package org.ciat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.ciat.export.CountExporter;
import org.ciat.model.Basis;
import org.ciat.model.MapCounter;
import org.ciat.model.Utils;
import org.ciat.transform.CWRDBNormalizer;
import org.ciat.transform.GBIFNormalizer;
import org.ciat.transform.GenesysNormalizer;
import org.ciat.transform.Normalizer;

public class ExecNormalizer extends Executer {


	public static MapCounter totalRecords = new MapCounter();
	public static MapCounter totalUseful = new MapCounter();
	public static MapCounter totalGRecords = new MapCounter();
	public static MapCounter totalGUseful = new MapCounter();
	public static MapCounter totalHRecords = new MapCounter();
	public static MapCounter totalHUseful = new MapCounter();
	public static MapCounter totalPost1950 = new MapCounter();
	public static MapCounter totalPre1950 = new MapCounter();

	public static void main(String[] args) {
		Executable app = new ExecNormalizer();
		app.run();
	}

	public void run() {

		log("Starting process");

		File normalized = new File("data1.csv");
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(normalized)))) {
			String header = (new Normalizer()).getHeader();
			writer.println(header);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Reduce and normalize
		log("Normalizing GBIF data");
		Normalizer gbifNormalizer = new GBIFNormalizer();
		gbifNormalizer.process(new File("gbif.csv"), normalized);
		System.gc();

		// filter Genesys data
		log("Normalizing Genesys data");
		Normalizer genesysNormalizer = new GenesysNormalizer();
		genesysNormalizer.process(new File("genesys.csv"), normalized);
		System.gc();

		// filter CWR data
		log("Normalizing CWR data");
		Normalizer cwrdbNormalizer = new CWRDBNormalizer();
		cwrdbNormalizer.process(new File("cwr.csv"), normalized);
		System.gc();
		
		// export counters
		log("Exporting counters");
		CountExporter countExporter = new CountExporter();
		countExporter.process();
		System.gc();


	}

	public static void updateCounters(String taxonkey, boolean useful, String year, Basis basis) {
		ExecNormalizer.totalRecords.increase(taxonkey);
		
		if (Utils.isNumeric(year)) {
			Integer yearNumber = Integer.parseInt(year);
			if (yearNumber >= Normalizer.YEAR) {
				ExecNormalizer.totalPost1950.increase(taxonkey);
			} else {
				ExecNormalizer.totalPre1950.increase(taxonkey);
			}
		} else {
			ExecNormalizer.totalPre1950.increase(taxonkey);
		}

		if (basis.equals(Basis.G)) {
			ExecNormalizer.totalGRecords.increase(taxonkey);
		} else {
			ExecNormalizer.totalHRecords.increase(taxonkey);
		}

		if (useful) {

			ExecNormalizer.totalUseful.increase(taxonkey);
			if (basis.equals(Basis.G)) {
				ExecNormalizer.totalGUseful.increase(taxonkey);
			} else {
				ExecNormalizer.totalHUseful.increase(taxonkey);
			}
		}

	}



}
