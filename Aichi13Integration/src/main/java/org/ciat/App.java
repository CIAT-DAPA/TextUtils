package org.ciat;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ciat.export.CWRDBToMaxent;
import org.ciat.export.GBIFToMaxent;
import org.ciat.export.GenesysToMaxent;
import org.ciat.transform.CWRDBNormalizer;
import org.ciat.transform.LineFilterer;
import org.ciat.transform.NativenessFilterer;

public class App {

	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static void main(String[] args) {
		showTimestamp();

		// Reduce and normalize
		// filter GBIF data
		LineFilterer lineFilterer = new LineFilterer();
		lineFilterer.process(new File("gbif.csv"),new File("gbif2.csv"));
		showTimestamp();
		NativenessFilterer nativenessFilterer = new NativenessFilterer();
		nativenessFilterer.process(new File("gbif2.csv"),new File("gbif3.csv"));
		showTimestamp();
		
		// filter genesys data		
		nativenessFilterer = new NativenessFilterer();
		nativenessFilterer.process(new File("genesys.csv"),new File("genesys2.csv"));
		showTimestamp();
		
		// filter and normalize CWR data
		CWRDBNormalizer cwrdbNormalizer_ = new CWRDBNormalizer();
		cwrdbNormalizer_.process(new File("cwr.csv"),new File("cwr2.csv"));
		showTimestamp();
		nativenessFilterer = new NativenessFilterer();
		nativenessFilterer.process(new File("cwr2.csv"),new File("cwr3.csv"));
		showTimestamp();
		

		// convert GBIF to Maxen format
		GBIFToMaxent gbifToMaxent = new GBIFToMaxent();
		//gbifToMaxent.process(new File("gbif3.csv"));
		showTimestamp();

		// convert Genesys to Maxen format
		GenesysToMaxent genesysToMaxent = new GenesysToMaxent();
		genesysToMaxent.process(new File("genesys2.csv"));
		showTimestamp();
		
		// convert Genesys to Maxen format
		CWRDBToMaxent cwrdbToMaxent = new CWRDBToMaxent();
		//cwrdbToMaxent.process(new File("cwr2.csv"));
		showTimestamp();

	}

	private static void showTimestamp() {
		Date date = new Date();
		System.out.println();
		System.out.println(dateFormat.format(date));
	}
	


}
