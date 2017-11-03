import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/*The precipitation data is on a 2.5 degree grid. 
 * In terms of the center point of each grid box, the first latitude is 88.75 N, the next is 86.25 N, and so on until it reaches 58.75 S. It stops there. 
 * There are 60 latitudes. Within each latitude the longitudes start at -178.75 E, then -176.25 E, and so on until 178.75 W. There are 144 longitudes. 

The temperature data is on a 2 degree grid. 
The first latitude is 81 N, the next is 79 N, and so on until it reaches 59 S. It stops there. 
There are 71 latitudes. Within each latitude, the longitudes start at -179 E, then -177 E, and so on until 179 W. There are 180 longitudes.
*/

public class IRItoEnvelope {
	private final File inputFolder = new File("input_test");
	private final File outputFolder = new File("output");
	private final String type = "record";
	private final String index = "iri";
	private int x;
	private int y;

	private final BoundingBox bbox = new BoundingBox(88.75, -178.75, -58.75, 178.75);
	private final double cellSize = 2.5;

	public static void main(String[] args) {
		IRItoEnvelope app = new IRItoEnvelope();
		app.transform();

	}

	private void transform() {

		x = (int) (Math.abs((bbox.getLeftLon() + 1000) - (bbox.getRightLon() + 1000)) / cellSize);
		y = (int) (Math.abs((bbox.getTopLat() + 1000) - (bbox.getBottonLat() + 1000)) / cellSize);
		String[][] grid = new String[x][y];
		if (inputFolder.exists() && inputFolder.isDirectory()) {
			for (File input : inputFolder.listFiles()) {
				File output = new File(outputFolder.getName() + File.separatorChar + input.getName() + ".json");
				addLocation(grid);
				addValues(input, grid);
				writeRecords(output, grid);
			}
		}

	}

	private void addLocation(String[][] grid) {
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				int id = i * 1000 + j;
				grid[i][j] = "{\"index\":{\"_index\":\"" + index + "\",\"_type\":\"" + type + "\",\"_id\":" + id + "}}"
						+ "\r\n";
				double topLat = bbox.getTopLat() - cellSize * j;
				double leftLon = bbox.getLeftLon() + cellSize * i;
				double bottonLat = bbox.getTopLat() - cellSize * (j + 1);
				double rightLon = bbox.getLeftLon() + cellSize * (i + 1);

				grid[i][j] += "{\"id\":" + id + ",\"centroid\" :\"" + (topLat - (cellSize / 2)) + ", "
						+ (leftLon + (cellSize / 2)) + "\"";
				grid[i][j] += ",\"region\":{\"type\":\"envelope\",\"coordinates\":[[" + leftLon + ", " + topLat
						+ "],[" + rightLon + "," + bottonLat + "]]}";
			}
		}

	}

	private void writeRecords(File output, String[][] grid) {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output, true)))) {

			for (int i = 0; i < x; i++) {
				for (int j = 0; j < y; j++) {
					grid[i][j] += "}";
					//System.out.println(grid[i][j]);
					writer.println(grid[i][j]);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void addValues(File input, String[][] grid) {

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"))) {

			/* header */
			String line = reader.readLine();

			line = reader.readLine();
			String varName = "";
			int indexY = 0;
			int indexX = 0;
			while (line != null) {
				if (line.startsWith("9") || line.startsWith("-") || line.startsWith("8") || line.startsWith("7")
						|| line.startsWith("6") || line.startsWith("7") || line.startsWith("6") || line.startsWith("5")
						|| line.startsWith("4") || line.startsWith("3") || line.startsWith("2")
						|| line.startsWith("1")) {

				} else {
					varName = line.replaceAll(" ", "_");
					indexY = 0;
					indexX = 0;
				}
				line = reader.readLine();
			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + input.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
