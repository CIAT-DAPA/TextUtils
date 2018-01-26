package org.ciat.transform;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.ciat.model.FileProgressBar;
import org.ciat.model.Utils;
import org.json.JSONObject;

public class CWRDBNormalizer {

	private Map<String, Integer> colIndex = new LinkedHashMap<String, Integer>();
	private Map<String, String> speciesKeys = new TreeMap<String, String>();
	private static final String INPUT_SEPARATOR = "\\|";
	private static final String OUTPUT_SEPARATOR = "\t";

	public void process(File input, File output) {

		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)));
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();
			if (colIndex.isEmpty()) {
				colIndex = Utils.getColumnsIndex(line, INPUT_SEPARATOR);
			}
			/* */

			/* progress bar */
			FileProgressBar bar = new FileProgressBar(input.length());
			/* */

			line = reader.readLine();
			String past = "";
			while (line != null) {

				String normal = normalize(line);
				if (normal != null && !normal.equals(past)) {
					writer.println(normal);
					past = normal;
				}

				/* show progress */
				bar.update(line.length());
				/* */

				line = reader.readLine();

			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + input.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String normalize(String line) {
		line = line.replace("\"", "");
		String[] values = line.split(INPUT_SEPARATOR);
		if (values.length == colIndex.size()) {
			if (!values[colIndex.get("final_origin_stat")].equals("introduced")) {
				if (values[colIndex.get("coord_source")].equals("original")
						|| values[colIndex.get("coord_source")].equals("georef")) {
					if (values[colIndex.get("visibility")].equals("1")) {
						if (values[colIndex.get("source")].equals("G") || values[colIndex.get("source")].equals("H")) {

							String date = values[colIndex.get("colldate")];
							if (date.length() > 3) {
								date = date.substring(0, 4);
								if (isNumeric(date)) {

									int year = Integer.parseInt(date);
									String lon = values[colIndex.get("final_lon")];
									String lat = values[colIndex.get("final_lat")];
									String country = values[colIndex.get("final_iso2")];
									String type = values[colIndex.get("source")];
									if (year > 1949 && isNumeric(lon) && isNumeric(lat) && country.length() == 2) {

										String taxonKey = fetchTaxonInfo(values[colIndex.get("taxon_final")]);
										if (taxonKey != null) {
											String result = taxonKey + OUTPUT_SEPARATOR + lon + OUTPUT_SEPARATOR + lat
													+ OUTPUT_SEPARATOR + country + OUTPUT_SEPARATOR + type;
											return result;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	private String fetchTaxonInfo(String name) {

		// check first in the Map

		String result = speciesKeys.get(name);
		if (result != null) {
			return result;
		} else {
			result = "";
		}

		// make connection

		URLConnection urlc;
		try {
			URL url = new URL("http://api.gbif.org/v1/species/match?kingdom=Plantae&name=" + name + "");

			urlc = url.openConnection();
			// use post mode
			urlc.setDoOutput(true);
			urlc.setAllowUserInteraction(false);

			// send query
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()))) {

				// get result
				String json = br.readLine();
				String keyField = "usageKey";
				String rankField = "rank";

				JSONObject object = new JSONObject(json);
				if (object.has(rankField) && object.has(keyField)) {
					String rank = object.get(rankField) + "";
					// check if the taxon is an specie or subspecie
					if (rank.contains("SPECIE")) {
						String value = object.get(keyField) + "";
						value = value.replaceAll("\n", "");
						value = value.replaceAll("\r", "");
						value = value.replaceAll(INPUT_SEPARATOR, " ");
						result += value;
						// add result in the Map
						speciesKeys.put(name, value);
						return result;
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static boolean isNumeric(String str) {
		if (str == null) {
			return false;
		}
		try {
			@SuppressWarnings("unused")
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

}
