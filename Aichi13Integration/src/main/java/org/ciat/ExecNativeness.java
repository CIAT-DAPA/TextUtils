package org.ciat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.ciat.transform.NativenessMarker;
import org.ciat.transform.Normalizer;

public class ExecNativeness extends Executer {

	public static void main(String[] args) {
		ExecNativeness app = new ExecNativeness();
		app.run();
	}

	public void run() {

		log("Marking nativeness");
		File normalized = new File("data1.csv");
		File nativenessed = new File("data2.csv");
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(nativenessed)))) {
			String header = (new Normalizer()).getHeader() + Normalizer.SEPARATOR + "origin";
			writer.println(header);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		NativenessMarker nativenessMarker = new NativenessMarker();
		nativenessMarker.process(normalized, nativenessed);
		System.gc();

	}

}
