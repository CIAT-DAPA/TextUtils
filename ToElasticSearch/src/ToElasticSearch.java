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
import java.util.Map;


public class ToElasticSearch {

	private Map<String, Integer> colIndex;
	private static final String SEPARATOR = "\t";
	private static final String LINE_JUMP = "\r\n";

	public static void main(String[] args) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date));

		String fileName = "data.csv";
		if (args.length > 0) {
			fileName = args[0];
		} else {
			System.out.println("File not provided in arguments, using "+ fileName +" as default");
		}

		ToElasticSearch app = new ToElasticSearch();
		app.extract(fileName);

		date = new Date();
		System.out.println();
		System.out.println(dateFormat.format(date));
	}

	private void extract(String fileName) {

		File input = new File(fileName);
		File output = new File("data.json");

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
			long done = line.length();
			int lineNumber = 0;
			System.out.println("Reading " + input.length() / 1024 + "KB");
			System.out.println("Updating progress each " + dimensionality + "KB read");
			/* */

			line = reader.readLine();
			while (line != null) {
				line += "\t ";
				writer.write(getAsESRecord(line));


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

	/** Getting record as ElasticSearch record */
	private String getAsESRecord(String line) {
		String[] values = line.split(SEPARATOR);
		String json = ("{\"index\":{\"_index\":\"gbif" + "\",\"_type\":\"record\",\"_id\":"
				+ values[colIndex.get("gbifid")] + "}}");
		json += LINE_JUMP;

		json += "{";
		for (String column : colIndex.keySet()) {
				String value = values[colIndex.get(column)];
				if (!column.equals("gbifid") && !column.equals("day") && !column.equals("month")
						&& !column.equals("decimallongitude") && !column.equals("decimallatitude")
						&& !column.equals("year") && !column.equals("taxonkey") && !column.equals("specieskey")
						&& !column.equals("coordinateuncertaintyinmeters") && !column.equals("coordinateprecision")
						&& !column.equals("elevation") && !column.equals("elevationaccuracy") && !column.equals("depth")
						&& !column.equals("depthaccuracy")) {
					value = value.replaceAll("\"", "'");
					value = "\"" + value + "\"";
				} else {
					if (value.equals("")) {
						continue;
					}
				}

				json += ("\"" + column + "\":" + value + ",");
			}
		

		if (!values[colIndex.get("decimallatitude")].equals("")
				&& !values[colIndex.get("decimallongitude")].equals("")) {
			// include geocoordinates
			
			json += ("\"" + "geolocation" + "\":" + "{\"lat\": " + values[colIndex.get("decimallatitude")]
					+ ",\"lon\":" + values[colIndex.get("decimallongitude")] + "}");
		} else {
			json = json.substring(0, json.length()-1); // removing last comma
		}

		json += "}";
		json += LINE_JUMP;

		return json;

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
