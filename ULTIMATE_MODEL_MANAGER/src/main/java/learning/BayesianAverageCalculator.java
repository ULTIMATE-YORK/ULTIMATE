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
            // Read first two lines
            C = Integer.parseInt(br.readLine().split("=")[1].trim());
            prior = Double.parseDouble(br.readLine().split("=")[1].trim());

            // Read remaining lines as value_i
            String line;
            while ((line = br.readLine()) != null) {
                values.add(Double.parseDouble(line.trim()));
            }
        } 

        int N = values.size();
        if (N == 0) return prior; // Avoid division by zero

        // Compute mean of value_i
        double meanValue = values.stream().mapToDouble(Double::doubleValue).sum() / N;

        // Compute Bayesian average
        return (C / (double) (C + N)) * prior + (N / (double) (C + N)) * meanValue;
    }
    
    public static double computeBayesianAverageRate(String filename, String character) throws NumberFormatException, IOException {
		double bayes = computeBayesianAverage(filename);
		if (bayes == 0.0) {
			return 0.0;
		}
        return 1/bayes;
    }
}
