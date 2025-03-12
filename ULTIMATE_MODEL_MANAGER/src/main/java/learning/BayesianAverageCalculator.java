package learning;

import java.io.*;
import java.util.*;

public class BayesianAverageCalculator {
	
    public static void main(String[] args) {
    	// Test Bayesian
        //String filename = "data.txt";
        //double result = computeBayesianAverage(filename);
        //System.out.println("Computed Bayesian Average: " + result);
        // Test Bayesian Rate
        //double resultRate = computeBayesianAverageRate(filename, "\n");
        //System.out.println("Computed Bayesian Rate: " + resultRate);
    }

    public static double computeBayesianAverage(String filename) throws NumberFormatException, IOException {
        int C = 0;
        double prior = 0.0;
        List<Double> values = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            // Read the CSV line
            String line = br.readLine();
            if (line == null || line.trim().isEmpty()) {
                throw new IOException("The file is empty or not properly formatted.");
            }
            // Split by comma
            String[] tokens = line.split(",");
            if (tokens.length < 2) {
                throw new IOException("CSV file must contain at least c and prior values.");
            }
            
            // Parse the first two tokens (e.g., "c=10", " prior=3.5")
            C = Integer.parseInt(tokens[0].split("=")[1].trim());
            prior = Double.parseDouble(tokens[1].split("=")[1].trim());
            
            // Parse the remaining tokens as numeric values
            for (int i = 2; i < tokens.length; i++) {
                values.add(Double.parseDouble(tokens[i].trim()));
            }
        }
        
        int N = values.size();
        if (N == 0) return prior; // Avoid division by zero

        // Compute mean of the values
        double meanValue = values.stream().mapToDouble(Double::doubleValue).sum() / N;
        
        // Compute Bayesian average
        return (C / (double) (C + N)) * prior + (N / (double) (C + N)) * meanValue;
    }

    
    public static double computeBayesianAverageRate(String filename) throws NumberFormatException, IOException {
		double bayes = computeBayesianAverage(filename);
		if (bayes == 0.0) {
			return 0.0;
		}
        return 1/bayes;
    }
}
