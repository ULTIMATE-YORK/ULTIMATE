package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Model;

public class FileUtils {
	
	public static final String[] VALID_PRISM_FILE_EXTENSIONS = {"*.ctmc", "*.dtmc", "*.pomdp", "*.prism", "*.mdp"};
	public static final String VALID_ULT_FILE_EXTENSIONS = "*.ultimate";
	
	/**
	 * Check if a file is an existing prism file by checking the file exists and ends with a prism model file extension
	 * 
	 * @param filePath
	 * @return boolean
	 * @throws IOException 
	 */
	public static boolean isPrismFile(String filePath) throws IOException {
		if (isFile(filePath) && isPrismModelFile(filePath)) {
			return true;
		}
		else {
			throw new IOException("File at " + filePath + " does not exist or is not a prism model file");
		}
	}
	
	/*
	 * Check if a file is an existing ultimate file by checking the file extension
	 * 
	 * @param projectPath
	 * @return boolean
	 */
	public static boolean isUltimateFile(String projectPath) throws IOException {
        if (isFile(projectPath) && projectPath.toLowerCase().endsWith(".ultimate")) {
            return true;
        }
        else {
            throw new IOException("File does not exist or is not an ultimate file");
        }
	}
	
	/**
	 * Check if a file is an existing file
	 * 
	 * @param filePath
	 * @return boolean
	 */
	public static boolean isFile(String filePath) {
		// check file exists
		return Files.exists(Paths.get(filePath));
	}
	
	/*
	 * Returns the name of a file without the file extension
	 * @param filePath
	 * @return String the file name without the file extension
	 */
	public static String removePrismFileExtension(String filePath) throws IOException {
		if (isPrismFile(filePath)) {
	        Path path = Paths.get(filePath);
	        String fileName = path.getFileName().toString(); // Get "c.prism"
	        
	        int lastDotIndex = fileName.lastIndexOf('.');
	        return (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
		}
		return null;
	}
	
	public static String removeFullPathPrism(String filePath) throws IOException {
		if (isPrismFile(filePath)) {
	        Path path = Paths.get(filePath);
	        return path.getFileName().toString(); // Get "c.prism"
		}
		return null;
	}
	
	/*
	 * Returns the name of a file without the file extension
	 * @param filePath
	 * @return String the file name without the file extension
	 */
	public static String removeUltimateFileExtension(String filePath) throws IOException {
		if (isUltimateFile(filePath)) {
	        Path path = Paths.get(filePath);
	        String fileName = path.getFileName().toString(); // Get "c.prism"
	        
	        int lastDotIndex = fileName.lastIndexOf('.');
	        return (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
		}
		return null;
	}
	
	public static String getFileContent(String filePath) throws IOException {
		if (isFile(filePath)) {
			return Files.readString(Paths.get(filePath));
		}
		return null;
	}
	
	// PRIVATE METHODS
	
	/*
	 * Check if a file is an existing prism model file by checking the file extension
	 * 
	 * @param filePath
	 * @return boolean
	 */
	private static boolean isPrismModelFile(String filePath) {
		String[] extensions = new String[] {".prism", ".ctmc", ".dtmc", ".mdp", ".pomdp"};
        for (String ext : extensions) {
            if (filePath.toLowerCase().endsWith(ext.toLowerCase())) {
                return true;
            }
        }
        return false;
	}
	
   public static void updateModelFileResults(Model model, HashMap<String, Double> constants) {
        String filePath = model.getFilePath();
        
        try {
            // Read all lines from the file
            Path path = Paths.get(filePath);
            StringBuilder updatedContent = new StringBuilder();

            for (String line : Files.readAllLines(path)) {
                String updatedLine = line;

                // Check if the line contains the pattern "const double NAME;"
                for (String key : constants.keySet()) {
                    String pattern = "const double " + key + ";";
                    if (line.contains(pattern)) {
                        double value = constants.get(key);
                        updatedLine = "const double " + key + " = " + value + ";";
                        break; // Stop checking once a match is found for this line
                    }
                }

                updatedContent.append(updatedLine).append(System.lineSeparator());
            }

            // Write the updated content back to the file
            Files.write(path, updatedContent.toString().getBytes());

        } catch (IOException e) {
            System.err.println("Error updating model file: " + e.getMessage());
        }
    }
   
   public static void writeParametersToFile(String filePath, HashMap<String, Double> constants) {       
       try {
           // Read all lines from the file
           Path path = Paths.get(filePath);
           StringBuilder updatedContent = new StringBuilder();

           // Process each line from the file
           for (String line : Files.readAllLines(path)) {
               String updatedLine = line;

               // For each key in the constants map, use regex to find a match for "const <type> key;"
               for (String key : constants.keySet()) {
                   // The regex pattern captures the type in group(1)
                   String regex = "const\\s+(\\S+)\\s+" + Pattern.quote(key) + "\\s*;";
                   Pattern pattern = Pattern.compile(regex);
                   Matcher matcher = pattern.matcher(line);
                   if (matcher.find()) {
                       String type = matcher.group(1);
                       double value = constants.get(key);
                       
                       // If type is "int", cast the value to int before inserting it.
                       if ("int".equals(type)) {
                           updatedLine = "const " + type + " " + key + " = " + ((int) value) + ";";
                       } else {
                           updatedLine = "const " + type + " " + key + " = " + value + ";";
                       }
                       break; // Stop checking keys for this line once a match is found.
                   }
               }
               updatedContent.append(updatedLine).append(System.lineSeparator());
           }

           // Write the updated content back to the file
           Files.write(path, updatedContent.toString().getBytes());
       } catch (IOException e) {
           System.err.println("Error updating model file: " + e.getMessage());
       }
   }

   /**
    * Reads and returns the first line of a file
    * 
    * @param filePath The path to the file
    * @return The first line of the file, or null if the file couldn't be read
    * @throws IOException If there's an error reading the file
    */
   public static String readFirstLine(String filePath) throws IOException {
       try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
           return reader.readLine();
       }
   }
	
   /**
    * Reads the entire contents of a file as a string
    * 
    * @param filePath The path to the file
    * @return The contents of the file as a string, or null if the file couldn't be read
    * @throws IOException If there's an error reading the file
    */
   public static String readFileAsString(String filePath) throws IOException {
       StringBuilder contentBuilder = new StringBuilder();
       try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
           String line;
           while ((line = reader.readLine()) != null) {
               contentBuilder.append(line);
               contentBuilder.append(System.lineSeparator());
           }
       }
       return contentBuilder.toString();
   }
}

