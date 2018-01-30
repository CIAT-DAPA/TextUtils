package org.ciat.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONObject;

public class TaxonFinder {
	
	private static TaxonFinder instance = null;
	private Map<String, String> speciesKeys = new TreeMap<String, String>();
	
	public String fetchTaxonInfo(String name) {

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
			URL url = new URL("http://api.gbif.org/v1/species/match?kingdom=Plantae&name=" + URLEncoder.encode(name,"UTF-8") + "");

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

	public static TaxonFinder getInstance() {
	      if(instance == null) {
	          instance = new TaxonFinder();
	       }
	       return instance;
	}

}
