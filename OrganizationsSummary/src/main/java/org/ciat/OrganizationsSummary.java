package org.ciat;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.json.JSONObject;

public class OrganizationsSummary {

	private static final String SEPARATOR = "\t";

	public static void main(String[] args) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date));

		String fileName = "orgs_keys.csv";
		if (args.length > 0) {
			fileName = args[0];
		} else {
			System.out.println("Processing " + fileName);
		}

		OrganizationsSummary app = new OrganizationsSummary();
		app.extract(fileName);

		date = new Date();
		System.out.println();
		System.out.println(dateFormat.format(date));
	}

	private void extract(String fileName) {

		File input = new File(fileName);
		File output = new File("orgs.csv");

		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)));
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();
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

			String head = "institutioncode" + SEPARATOR + "publishingorgkey" + SEPARATOR;
			for (String keyfield : targetFields) {
				head += keyfield + SEPARATOR;
			}
			writer.println(head);

			line = reader.readLine();
			while (line != null) {
				line += SEPARATOR + " ";
				String[] values = line.split(SEPARATOR);
				String code = values[0];
				String pub = values[1];
				if (!publishers.contains(pub)) {
					writer.println(code + SEPARATOR + pub + SEPARATOR + fetchInfo(pub));
					publishers.add(pub);
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


	private String[] targetFields = { "key", "endorsingNodeKey", "title", "description", "language", "email", "phone", "homepage", "city",
			"country", "postalCode", "latitude", "longitude", "numPublishedDatasets", "created", "modified" };

	private String fetchInfo(String key) {

		// make connection

		URLConnection urlc;
		try {
			URL url = new URL("http://api.gbif.org/v1/organization/" + key + "");

			urlc = url.openConnection();
			// use post mode
			urlc.setDoOutput(true);
			urlc.setAllowUserInteraction(false);

			// send query
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()))) {

				// get result
				String json = br.readLine();
				String result = "";
				for (String keyfield : targetFields) {

					JSONObject object = new JSONObject(json);
					if (object.has(keyfield)) {
						String value = object.get(keyfield) + "";
						value = value.replaceAll("\n", " ");
						value = value.replaceAll("\r", " ");
						value = value.replaceAll(SEPARATOR, " ");
						result += value + SEPARATOR;
					} else {
						result += "" + SEPARATOR;
					}

				}
				return result;

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

}
