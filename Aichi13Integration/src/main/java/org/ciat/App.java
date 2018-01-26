package org.ciat;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ciat.export.CWRDBToMaxent;
import org.ciat.export.GBIFToMaxent;
import org.ciat.export.GenesysToMaxent;
import org.ciat.transform.CWRDBNormalizer;
import org.ciat.transform.GBIFNormalizer;
import org.ciat.transform.NativenessFilterer;

public class App {

	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static void main(String[] args) {
		log("Starting process");

		// Reduce and normalize
		// filter GBIF data
		log("Filtering GBIF data");
		GBIFNormalizer lineFilterer = new GBIFNormalizer();
		lineFilterer.process(new File("gbif.csv"),new File("gbif2.csv"));
		log("Filtering GBIF for only native records");
		NativenessFilterer nativenessFilterer = new NativenessFilterer();
		nativenessFilterer.process(new File("gbif2.csv"),new File("gbif3.csv"));
		// convert GBIF to Maxen format
		log("GBIF to Maxent");
		GBIFToMaxent gbifToMaxent = new GBIFToMaxent();
		gbifToMaxent.process(new File("gbif3.csv"));
		
		// filter genesys data		
		log("Filtering Genesys for only native records");
		nativenessFilterer = new NativenessFilterer();
		nativenessFilterer.process(new File("genesys.csv"),new File("genesys2.csv"));
		// convert Genesys to Maxen format
		log("Genesys to Maxent");
		GenesysToMaxent genesysToMaxent = new GenesysToMaxent();
		genesysToMaxent.process(new File("genesys2.csv"));
		
		
		// filter and normalize CWR data
		log("Normalizing CWR taxonomy");
		CWRDBNormalizer cwrdbNormalizer_ = new CWRDBNormalizer();
		cwrdbNormalizer_.process(new File("cwr.csv"),new File("cwr2.csv"));
		log("Filtering CWR for only native records");
		nativenessFilterer = new NativenessFilterer();
		nativenessFilterer.process(new File("cwr2.csv"),new File("cwr3.csv"));
		// convert CWR to Maxen format
		log("CWR to Maxent");
		CWRDBToMaxent cwrdbToMaxent = new CWRDBToMaxent();
		cwrdbToMaxent.process(new File("cwr2.csv"));
		
		log("Process finished");

	}
	
	private static void log(String message){
		System.out.println();
		System.out.println(getTimestamp()+" "+message);	}
	

	private static String getTimestamp() {
		Date date = new Date();
		return dateFormat.format(date);
	}
	


}
