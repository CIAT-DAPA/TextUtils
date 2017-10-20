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

public class OrganizationsFinder {

	private Map<String, Integer> colIndex;
	private Set<String> institutions;
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

		OrganizationsFinder app = new OrganizationsFinder();
		app.extract(fileName);

		date = new Date();
		System.out.println();
		System.out.println(dateFormat.format(date));
	}

	private void extract(String fileName) {

		File input = new File(fileName);
		File output = new File("orgs.csv");

		File instFile = new File("institutions.txt");
		institutions = loadFilters(instFile);

		try (BufferedWriter writer = new BufferedWriter(
				new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8")));
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();
			colIndex = getColumnsIndex(line);
			/* */

			/* progress bar */
			ProgressBar bar = new ProgressBar();
			int exp = (int) Math.ceil((input.length() + "").length()) + 1;
			int dimensionality = (int) Math.pow(2, exp);
			int total = Math.toIntExact(input.length() / dimensionality);
			long done = line.length();
			int lineNumber = 0;
			System.out.println("Reading " + input.length() / 1024 + "KB");
			System.out.println("Updating progress each " + dimensionality + "KB read");
			/* */

			Set<String> publishers = new LinkedHashSet<String>();
			line = reader.readLine();
			while (line != null) {
				line += "\t ";
				String code=isTarget(line);
				if (code!=null) {
					String pub = getPublisher(line);
					if (!publishers.contains(pub)) {
						writer.write(code+SEPARATOR+pub+LINE_JUMP);
						publishers.add(pub);
					}
				}

				/* show progress */
				done += line.length();
				if (++lineNumber % dimensionality == 0) {
					bar.update(Math.toIntExact(done / dimensionality), total);
				}
				/* */

				line = reader.readLine();

			}
			bar.update(Math.toIntExact(done / dimensionality), total);

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + input.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Getting only taxon and geocoordinates */
	@SuppressWarnings("unused")
	private String getGeospatialInfo(String line) {
		String[] values = line.split(SEPARATOR);
		return values[colIndex.get("scientificname")] + SEPARATOR + values[colIndex.get("decimallatitude")] + SEPARATOR
				+ values[colIndex.get("decimallongitude")];
	}

	private String getPublisher(String line) {
		String[] values = line.split(SEPARATOR);
		return values[colIndex.get("publishingorgkey")];
	}

	private String isTarget(String line) {
		String[] values = line.split(SEPARATOR);
		for (String institution : institutions) {
			if (values[colIndex.get("institutioncode")].equals(institution)) {
				return institution;
			}
		}

		return null;
	}

	private Set<String> loadFilters(File vocabularyFile) {
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
