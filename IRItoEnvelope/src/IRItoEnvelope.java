import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

/*The precipitation data is on a 2.5 degree grid. 
 * In terms of the center point of each grid box, the first latitude is 88.75 N, the next is 86.25 N, and so on until it reaches 58.75 S. It stops there. 
 * There are 60 latitudes. Within each latitude the longitudes start at -178.75 E, then -176.25 E, and so on until 178.75 W. There are 144 longitudes. 

The temperature data is on a 2 degree grid. 
The first latitude is 81 N, the next is 79 N, and so on until it reaches 59 S. It stops there. 
There are 71 latitudes. Within each latitude, the longitudes start at -179 E, then -177 E, and so on until 179 W. There are 180 longitudes.
*/

public class IRItoEnvelope {
	private static final String NULL = "9.000";
	private final File inputFolder = new File("input");
	private final File outputFolder = new File("output");
	private final File mappingFile = new File(outputFolder.getName() + File.separatorChar + "mapping.json");
	private final File uploadFile = new File(outputFolder.getName() + File.separatorChar + "upload.bat");
	private final String type = "record";
	private final String index = "iri";
	private Set<String> properties = new LinkedHashSet<String>();

	public static void main(String[] args) {
		IRItoEnvelope app = new IRItoEnvelope();
		app.transform();

	}

	private void transform() {

		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(uploadFile, true)))) {

			writer.println("curl -k -i --raw -o " + mappingFile.getName()
					+ ".log -X PUT \"http://localhost:9200/iri\" -H \"Content-Type: application/json\" -H \"Host: localhost:9200\" --data-binary @"
					+ mappingFile.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (inputFolder.exists() && inputFolder.isDirectory()) {
			for (File input : inputFolder.listFiles()) {
				if (input.getName().indexOf("skill") == 0) {
					BoundingBox bbox;
					double cellSize;
					String variable = input.getName().substring(input.getName().length() - 7,
							input.getName().length() - 5);
					int season = Integer.parseInt(
							input.getName().substring(input.getName().length() - 2, input.getName().length()));
					switch (variable) {
					case "P1":
						bbox = new BoundingBox(88.75, -178.75, -58.75, 178.75);
						cellSize = 2.5;
						break;
					case "T1":
						bbox = new BoundingBox(81, -179, -59, 179);
						cellSize = 2;
						break;
					default:
						bbox = new BoundingBox(88.75, -178.75, -58.75, 178.75);
						cellSize = 2.5;
						break;
					}
					String[][] grid;
					int width = 1
							+ (int) (Math.abs((bbox.getLeftLon() + 1000) - (bbox.getRightLon() + 1000)) / cellSize);
					int height = 1
							+ (int) (Math.abs((bbox.getTopLat() + 1000) - (bbox.getBottonLat() + 1000)) / cellSize);
					grid = new String[height][width];
					File output = new File(outputFolder.getName() + File.separatorChar + input.getName() + ".json");
					addSeason(variable, season, grid, width, height);
					addLocation(grid, bbox, cellSize, width, height);
					addValues(input, grid);
					writeRecords(output, grid, width, height);
				}
			}
		}
		writeMapping();
	}

	private void addLocation(String[][] grid, BoundingBox bbox, double cellSize, int width, int height) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double topLat = bbox.getTopLat() - cellSize * j;
				double leftLon = bbox.getLeftLon() + cellSize * i;
				double bottonLat = bbox.getTopLat() - cellSize * (j + 1);
				double rightLon = bbox.getLeftLon() + cellSize * (i + 1);

				grid[j][i] += ",\"centroid\" :\"" + (topLat - (cellSize / 2)) + ", " + (leftLon + (cellSize / 2)) + "\"";
				grid[j][i] += ",\"region\":{\"type\":\"envelope\",\"coordinates\":[[" + leftLon + ", " + topLat + "],["
						+ rightLon + "," + bottonLat + "]]}";
			}
		}

	}

	private void addSeason(String variable, int season, String[][] grid, int width, int height) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				long id = Long.parseLong(((int)variable.charAt(0)) + "" + season + "" + (i * 100 + j));
				grid[j][i] = "{\"index\":{\"_index\":\"" + index + "\",\"_type\":\"" + type + "\",\"_id\":" + id + "}}"
						+ "\r\n";
				grid[j][i] += "{\"id\":" + id;
				grid[j][i] += ",\"variable\":\"" + variable + "\"";
				grid[j][i] += ",\"season\":" + season;
				grid[j][i] += ",\"variable\":\"" + variable + "\"";
			}
		}

	}

	private void addValues(File input, String[][] grid) {

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String varName = "";
			int indexY = 0;
			int indexX = 0;
			String line = reader.readLine();
			while (line != null) {
				// if it's a value line
				line = line.trim();
				if (line.startsWith("0") || line.startsWith("9") || line.startsWith("8") || line.startsWith("7")
						|| line.startsWith("6") || line.startsWith("7") || line.startsWith("6") || line.startsWith("5")
						|| line.startsWith("4") || line.startsWith("3") || line.startsWith("2") || line.startsWith("1")
						|| line.startsWith("-")) {
					line = line.replaceAll("-", " -").trim();
					String[] values = line.split(" ");
					for (indexX = 0; indexX < values.length; indexX++) {
						if (!values[indexX].equals(NULL)) {
							grid[indexY][indexX] += ",\"" + varName + "\":" + values[indexX];
						}
					}
					indexY++;
				} else {
					varName = line.replaceAll(" ", "_");
					indexY = 0;
					properties.add(asJSONProperty(varName, "double"));
				}
				line = reader.readLine();
			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + input.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void writeRecords(File output, String[][] grid, int width, int height) {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					grid[j][i] += "}";
					writer.println(grid[j][i]);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(uploadFile, true)))) {

			writer.println("curl -k -i --raw -o " + output.getName()
					+ ".log -X POST \"http://localhost:9200/iri/_bulk?pretty\" -H \"Content-Type: application/json\" -H \"Host: localhost:9200\" --data-binary @"
					+ output.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void writeMapping() {
		String mapping = "";
		mapping += "{\"mappings\":{\"record\":{\"properties\":{\"id\":{\"type\":\"integer\",\"index\":\"true\" }";
		mapping += "," + asJSONProperty("centroid", "geo_point");
		mapping += "," + asJSONProperty("region", "geo_shape");
		mapping += "," + asJSONProperty("season", "integer");
		mapping += "," + asJSONProperty("variable", "keyword");

		for (String property : properties) {
			mapping += "," + property;
		}

		mapping += "}}}}";

		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(mappingFile)))) {
			writer.println(mapping);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String asJSONProperty(String name, String type) {
		return "\"" + name + "\": {\"type\":\"" + type + "\"}";
	}
}
