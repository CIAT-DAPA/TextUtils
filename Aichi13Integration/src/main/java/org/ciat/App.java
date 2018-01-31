package org.ciat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ciat.export.CountExporter;
import org.ciat.export.Maxentnisizer;
import org.ciat.model.Basis;
import org.ciat.model.MapCounter;
import org.ciat.model.Utils;
import org.ciat.transform.CWRDBNormalizer;
import org.ciat.transform.GBIFNormalizer;
import org.ciat.transform.GenesysNormalizer;
import org.ciat.transform.NativenessMarker;
import org.ciat.transform.Normalizer;

public class App {

	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static MapCounter totalRecords = new MapCounter();
	public static MapCounter totalUseful = new MapCounter();
	public static MapCounter totalGRecords = new MapCounter();
	public static MapCounter totalGUseful = new MapCounter();
	public static MapCounter totalHRecords = new MapCounter();
	public static MapCounter totalHUseful = new MapCounter();
	public static MapCounter totalPost1950 = new MapCounter();
	public static MapCounter totalPre1950 = new MapCounter();

	public static void main(String[] args) {
		App app = new App();
		app.run();
	}

	private void run() {

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
		GBIFNormalizer lineFilterer = new GBIFNormalizer();
		lineFilterer.process(new File("gbif.csv"), normalized);

		// filter Genesys data
		log("Normalizing Genesys data");
		GenesysNormalizer genesysNormalizer = new GenesysNormalizer();
		genesysNormalizer.process(new File("genesys.csv"), normalized);

		// filter CWR data
		log("Normalizing CWR data");
		CWRDBNormalizer cwrdbNormalizer = new CWRDBNormalizer();
		cwrdbNormalizer.process(new File("cwr.csv"), normalized);

		File nativenessed = new File("data2.csv");
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(nativenessed)))) {
			String header = (new Normalizer()).getHeader() + Normalizer.SEPARATOR + "origin";
			writer.println(header);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		log("Marking nativeness");
		NativenessMarker nativenessMarker = new NativenessMarker();
		nativenessMarker.process(normalized, nativenessed);

		// convert to Maxent format
		log("Data to Maxent");
		Maxentnisizer maxentnisizer = new Maxentnisizer();
		maxentnisizer.process(nativenessed);

		// convert to Maxent format
		log("Data to Maxent");
		CountExporter countExporter = new CountExporter();
		countExporter.process();

		log("Process finished");

	}

	public static void updateCounters(String taxonkey, boolean useful, String year, Basis basis) {
		App.totalRecords.increase(taxonkey);
		
		if (Utils.isNumeric(year)) {
			Integer yearNumber = Integer.parseInt(year);
			if (yearNumber >= Normalizer.YEAR) {
				App.totalPost1950.increase(taxonkey);
			} else {
				App.totalPre1950.increase(taxonkey);
			}
		} else {
			App.totalPre1950.increase(taxonkey);
		}

		if (basis.equals(Basis.G)) {
			App.totalGRecords.increase(taxonkey);
		} else {
			App.totalHRecords.increase(taxonkey);
		}

		if (useful) {

			App.totalUseful.increase(taxonkey);
			if (basis.equals(Basis.G)) {
				App.totalGUseful.increase(taxonkey);
			} else {
				App.totalHUseful.increase(taxonkey);
			}
		}

	}

	private static void log(String message) {
		System.out.println();
		System.out.println(getTimestamp() + " " + message);
	}

	private static String getTimestamp() {
		Date date = new Date();
		return dateFormat.format(date);
	}

}
