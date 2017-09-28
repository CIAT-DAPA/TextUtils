import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ClearJSON {

	public static void main(String[] args) {
		if (args.length > 0) {
			File input = new File(args[0]);
			File output = new File(args[0]);
			String line ="";
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input)))) {
				
				line = reader.readLine();
				line=line.replace(",\"facets\":[]}", "");
				String head=line.split("\\[")[0];
				line=line.replace(head, "");
				
			} catch (FileNotFoundException e) {
				System.out.println("File not found " + input.getAbsolutePath());
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Cannot read " + input.getAbsolutePath());
			}
			
			input.delete();
			
			try (BufferedWriter writer = new BufferedWriter(new PrintWriter(output))) {
				
				writer.write(line);
				
			} catch (FileNotFoundException e) {
				System.out.println("File not found " + input.getAbsolutePath());
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Cannot read " + input.getAbsolutePath());
			}
		}else{
			System.out.println("File not provided in arguments");
		}

	}

}
