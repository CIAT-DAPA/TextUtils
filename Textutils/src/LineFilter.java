import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

public class LineFilter {

	public static void main(String[] args) {
		LineFilter app=new LineFilter();
		app.run();
	}
	
	private void run(){

		String jump = "\r\n";
		// if (args.length > 0) {
		File input = new File("file.csv");
		File filterSet = new File("filter.txt");
		File output = new File("file_filtered.csv");
		Set<String> filters = loadFilters(filterSet);

		try (BufferedWriter writer = new BufferedWriter(new PrintWriter(output));
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input)))) {

			String line = reader.readLine();
			while (line != null) {
				for (String filter : filters) {
					if (line.contains(filter)) {
						writer.write(line);
						writer.write(jump);
					}
				}
				line = reader.readLine();
			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + input.getAbsolutePath());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Cannot read " + input.getAbsolutePath());
		}

		// } else {
		// System.out.println("File not provided in arguments");
		// }

	
	}
	
	private Set<String> loadFilters(File filterSet){
		Set<String> filters = new LinkedHashSet<String>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filterSet)))) {

			String line = reader.readLine();
			while (line != null) {
				filters.add(line);
				line = reader.readLine();
			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found " + filterSet.getAbsolutePath());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Cannot read " + filterSet.getAbsolutePath());
		}
		return filters;
	}

}
