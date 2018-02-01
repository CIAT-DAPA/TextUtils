package org.ciat.view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.ciat.control.CWRDBNormalizer;
import org.ciat.control.CountExporter;
import org.ciat.control.GBIFNormalizer;
import org.ciat.control.GenesysNormalizer;
import org.ciat.control.Normalizable;

public class ExecNormalizer extends Executer {


	public static void main(String[] args) {
		Executable app = new ExecNormalizer();
		app.run();
	}

	public void run() {

		log("Starting process");

		File normalized = new File("data1.csv");
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(normalized)))) {
			String header = Normalizable.getHeader();
			writer.println(header);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Reduce and normalize
		log("Normalizing GBIF data");
		Normalizable gbifNormalizer = new GBIFNormalizer();
		gbifNormalizer.process(new File("gbif.csv"), normalized);
		System.gc();

		// filter Genesys data
		log("Normalizing Genesys data");
		Normalizable genesysNormalizer = new GenesysNormalizer();
		genesysNormalizer.process(new File("genesys.csv"), normalized);
		System.gc();

		// filter CWR data
		log("Normalizing CWR data");
		Normalizable cwrdbNormalizer = new CWRDBNormalizer();
		cwrdbNormalizer.process(new File("cwr.csv"), normalized);
		System.gc();
		
		// export counters
		log("Exporting counters");
		CountExporter countExporter = new CountExporter();
		countExporter.process();
		System.gc();


	}





}
