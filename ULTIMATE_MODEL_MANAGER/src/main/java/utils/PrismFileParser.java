package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrismFileParser {

    /**
     * Parses a file to extract parameters in the form "const <type> <name>".
     *
     * @param filePath the location of the file to parse
     * @return an array of strings, each representing a parameter in the form "const <type> <name>"
     * @throws IOException if an error occurs while reading the file
     */
    public List<String> parseFile(String filePath) throws IOException {
    	FileUtils.isPrismFile(filePath);         // check valid prism file
    	List<String> parameters = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Check if the line starts with "const" and doesn't contain an assignment (=)
                if (line.startsWith("const") && !line.contains("=")) {
                    // Extract the parameter declaration
                    String parameter = extractParameter(line);
                    if (parameter != null) {
                        parameters.add(parameter);
                    }
                }
            }
        }

        return parameters;
    }

    /**
     * Extracts a parameter in the form "const <type> <name>" from a line.
     *
     * @param line the line to process
     * @return the formatted parameter string, or null if the line is invalid
     */
    private String extractParameter(String line) {
        // Split the line into tokens
        String[] tokens = line.split("\\s+");
        if (tokens.length >= 3 && tokens[0].equals("const")) {
            //String type = tokens[1];
            String name = tokens[2];

            // Ensure the name ends with a semicolon and remove it
            if (name.endsWith(";")) {
                name = name.substring(0, name.length() - 1);
                return name;
            }
        }
        return null;
    }
}
