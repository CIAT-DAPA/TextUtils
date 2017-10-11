package control;

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

import model.ProgressBar;

public class LineFilter {

	private Map<String, Integer> colIndex;
	private Set<String> taxa;
	private static final String SEPARATOR = "\t";
	private static final String LINE_JUMP = "\r\n";
	private static final String ES_PREFIX = "record.";

	public static void main(String[] args) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date));

		String fileName = "file.csv";
		if (args.length > 0) {
			fileName = args[0];
		} else {
			System.out.println("File not provided in arguments, using file.csv as default");
		}

		LineFilter app = new LineFilter();
		app.extract(fileName);

		date = new Date();
		System.out.println();
		System.out.println(dateFormat.format(date));
	}

	private void extract(String fileName) {

		File input = new File(fileName);
		File output = new File("records.json");

		File taxaFile = new File("taxa.txt");
		taxa = loadFilters(taxaFile);

		try (BufferedWriter writer = new BufferedWriter(
				new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8")));
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();
			colIndex = getColumnsIndex(line);
			// writer.write(line);
			// writer.write(LINE_JUMP);
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

			line = reader.readLine();
			while (line != null) {
				if (isTarget(line)) {
					writer.write(getAsESRecord(line));
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

	/** Getting record as ElasticSearch record */
	private String getAsESRecord(String line) {
		String[] values = line.split(SEPARATOR);
		String json = ("{\"index\":{\"_index\":\"records" + "\",\"_type\":\"sampling\",\"_id\":"
				+ values[colIndex.get("gbifid")] + "}}");
		json += LINE_JUMP;

		json += "{";
		for (String column : colIndex.keySet()) {
			String value = values[colIndex.get(column)];
			if (!isNumeric(value)) {
				value = "\"" + value + "\"";
			}
			json += ("\"" + ES_PREFIX + column + "\":" + value + ",");
		}

		json += "}";
		json += LINE_JUMP;

		return json;

	}

	public static boolean isNumeric(String s) {
		// TODO check if format has exponential 'E'
		return s.matches("[-+]?\\d*\\.?\\d+");
	}

	private boolean isTarget(String line) {
		line += "\t ";
		String[] values = line.split(SEPARATOR);

		if (!line.contains("SPECIES")) {
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

		/* matching with taxa */

		for (String taxon : taxa) {
			if (values[colIndex.get("taxonkey")].equals(taxon)) {
				return true;
			}
		}

		return false;
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
