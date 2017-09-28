import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class LineFilter {

	public static void main(String[] args) {
		String jump = "\r\n";
		//if (args.length > 0) {
			File input = new File("file.csv");
			File output = new File("file_filtered.csv");
			String line = "";
			try (BufferedWriter writer = new BufferedWriter(new PrintWriter(output));
					BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input)))) {

				line = reader.readLine();
				while (line != null) {
					if (line.contains("a")) {
						writer.write(line);
						writer.write(jump);
					}
					line = reader.readLine();
				}

			} catch (FileNotFoundException e) {
				System.out.println("File not found " + input.getAbsolutePath());
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Cannot read " + input.getAbsolutePath());
			}

		//} else {
		//	System.out.println("File not provided in arguments");
		//}

	}

}
