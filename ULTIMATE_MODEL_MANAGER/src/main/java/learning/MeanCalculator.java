package learning;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class MeanCalculator {
    
	public static Double computeMean(String filename, String character) throws IOException, NumberFormatException {
        // Read file content as a single string
        String content = new String(Files.readAllBytes(Paths.get(filename)));

        // Split the content based on the provided character
        String[] values = content.split(character);

        // Convert to double and compute mean
        return Arrays.stream(values)
                .mapToDouble(Double::parseDouble)
                .average()
                .orElse(0.0); // Return 0.0 if no values found
    }
	
	
	public static Double computeMeanRate(String filename, String character) throws NumberFormatException, IOException {
		Double mean = computeMean(filename, character);
		if (mean == 0.0) {
			return 0.0;
		}
        return 1/mean;
    }

    public static void main(String[] args) {
    	// Test mean
        //String filename = "dataMean.txt";
        //String separator = "\n"; // Values are separated by newlines or other separation character
        //double mean = computeMean(filename, separator);
        //System.out.println("Mean: " + mean);
        // Test mean rate
        //double meanRate = computeMeanRate(filename, separator);
        //System.out.println("Mean Rate: " + meanRate);
    }
}
