package org.ciat;

import java.io.File;

import org.ciat.export.Maxentnisizer;

public class ExecMaxentnisizer extends Executer {



	public static void main(String[] args) {
		ExecMaxentnisizer app = new ExecMaxentnisizer();
		app.run();
	}

	public void run() {


		// convert to Maxent format
		log("Exporting data to Maxent");
		File nativenessed = new File("data2.csv");
		Maxentnisizer maxentnisizer = new Maxentnisizer();
		maxentnisizer.process(nativenessed);
		System.gc();



	}



}
