package org.ciat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TaxaSegregator {

	private Map<String, Integer> colIndex;
	private String[] colTarget = { "gbifid", "taxonkey", "scientificname", "decimallatitude", "decimallongitude",
			"basisofrecord" };

	private static final String SEPARATOR = "\t";
	private static final String LINE_JUMP = "\r\n";

	public static void main(String[] args) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date));

		String fileName = "gbif.csv";
		if (args.length > 0) {
			fileName = args[0];
		} else {
			System.out.println("File not provided in arguments, using " + fileName + " as default");
		}

		TaxaSegregator app = new TaxaSegregator();
		app.extract(fileName);

		date = new Date();
		System.out.println();
		System.out.println(dateFormat.format(date));
	}

	private void extract(String fileName) {

		File input = new File(fileName);

		File taxaFile = new File("taxa.txt");
		Set<String> taxa = loadVocabulary(taxaFile);

		/* progress bar */
		ProgressBar bar = new ProgressBar();
		int total = taxa.size();
		int done = 0;
		bar.update(done, total);
		/* */
		for (String taxon : taxa) {
			File output = new File("data//" + taxon + ".csv");
			try (BufferedWriter writer = new BufferedWriter(
					new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8")));
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

				/* header */
				String line = reader.readLine();
				colIndex = getColumnsIndex(line);
				String header = "";
				for (String col : colTarget) {
					header += col + SEPARATOR;
				}
				writer.write(header);
				writer.write(LINE_JUMP);
				/* */

				line = reader.readLine();
				while (line != null) {
					line += SEPARATOR + " ";
					String[] values = line.split(SEPARATOR);
					if (values[colIndex.get("taxonkey")].equals(taxon) && isUsable(values)) {
						writer.write(getTargeValues(values));
						writer.write(LINE_JUMP);
					}
					line = reader.readLine();

				}

			} catch (FileNotFoundException e) {
				System.out.println("File not found " + input.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			bar.update(done++, total);
		}
	}

	/** Getting only targeted values **/
	private String getTargeValues(String[] values) {
		String output = "";
		for (String col : colTarget) {
			output += values[colIndex.get(col)] + SEPARATOR;
		}
		return output;
	}

	private boolean isUsable(String[] values) {
		

		if (!values[colIndex.get("taxonrank")].contains("SPECIES")) {
			return false;
		}

		/* excluding issues */
		Set<String> issues = new LinkedHashSet<>();
		issues.add("COORDINATE_OUT_OF_RANGE");
		issues.add("COUNTRY_COORDINATE_MISMATCH");
		issues.add("ZERO_COORDINATE");

		for (String issue : issues) {
			if (values[colIndex.get("issue")].contains(issue)) {
				return false;
			}
		}
		
		if(values[colIndex.get("decimallatitude")].equals("")||values[colIndex.get("longitude")].equals("")){
			return false;
		}

		return true;
	}

	private Set<String> loadVocabulary(File vocabularyFile) {
		Set<String> filters = new LinkedHashSet<String>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(vocabularyFile)))) {

			String line = reader.readLine();
			while (line != null) {
				if (!line.isEmpty()) {
					filters.add(line);
				}
				line = reader.readLine();
			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + vocabularyFile.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("Cannot read " + vocabularyFile.getAbsolutePath());
		}
		return filters;
	}

	private Map<String, Integer> getColumnsIndex(String line) {
		Map<String, Integer> colIndex = new LinkedHashMap<String, Integer>();
		String[] columnNames = line.split(SEPARATOR);
		for (int i = 0; i < columnNames.length; i++) {
			colIndex.put(columnNames[i], i);
		}
		return colIndex;
	}

}
