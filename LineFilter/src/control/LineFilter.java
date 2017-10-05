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
	private static final String SEPARATOR = "\t";
	private static final String LINE_JUMP = "\r\n";

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
		File output = new File("file_filtered.csv");

		try (BufferedWriter writer = new BufferedWriter(
				new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8")));
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();
			colIndex = getColumnsIndex(line);
			writer.write(line);
			writer.write(LINE_JUMP);
			/* */

			/* progress bar */
			ProgressBar bar = new ProgressBar();
			int exp = (int) Math.ceil((input.length() + "").length()) + 1;
			int dimensionality = (int) Math.pow(2, exp);
			int total = Math.toIntExact(input.length() / dimensionality);
			int done = line.length();
			int lineNumber = 0;
			System.out.println("Reading " + input.length() / 1024 + "KB");
			System.out.println("Updating progress each " + dimensionality + "KB read");
			/* */

			line = reader.readLine();
			while (line != null) {
				if (isTarget(line)) {
					writer.write(line);
					writer.write(LINE_JUMP);
				}

				/* show progress */
				done += line.length();
				if (++lineNumber % dimensionality == 0) {
					bar.update(done / dimensionality, total);
				}
				/* */

				line = reader.readLine();

			}
			bar.update(done / dimensionality, total);

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + input.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isTarget(String line) {
		boolean flag = false;
		line += "\t ";
		String[] values = line.split(SEPARATOR);

		if (!line.contains("SPECIES")) {
			return false;
		}

		/* excluding issues */
		flag = false;
		Set<String> issues = new LinkedHashSet<>();
		issues.add("COORDINATE_OUT_OF_RANGE");
		issues.add("COUNTRY_COORDINATE_MISMATCH");
		issues.add("ZERO_COORDINATE");

		matchIssue: for (String issue : issues) {
			if (!values[colIndex.get("issue")].contains(issue)) {
				flag = true;
				break matchIssue;
			}
		}
		if (!flag) {
			return flag; // return false
		}
		/**/

		/* matching with taxa */
		flag = false;
		File taxaFile = new File("taxa.txt");
		Set<String> taxa = loadFilters(taxaFile);
		matchTaxa: for (String taxon : taxa) {
			if (values[colIndex.get("taxonkey")].equals(taxon)) {
				flag = true;
				break matchTaxa;
			}
		}
		if (!flag) {
			return flag; // return false in case taxon is not found
		}
		/**/

		return flag;
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
