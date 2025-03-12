package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing output from PRISM model checker
 */
public class PrismOutputParser {
    
    /**
     * Extracts a double result from PRISM output
     * 
     * @param output The PRISM output to parse
     * @return The parsed result, or null if parsing failed
     */
    public static Double getResult(String output) {
        if (output == null || output.isEmpty()) {
            return null;
        }
        
        // Regular expression to match the result in PRISM output
        Pattern pattern = Pattern.compile("Result: ([0-9.Ee\\-]+)");
        Matcher matcher = pattern.matcher(output);
        
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse PRISM result as double: " + matcher.group(1));
                e.printStackTrace();
                return null;
            }
        }
        
        System.err.println("No result found in PRISM output");
        return null;
    }
    
    /**
     * Extracts a rational function from PRISM parametric output
     * 
     * @param output The PRISM parametric output to parse
     * @return The rational function as a string, or null if parsing failed
     */
    public static String getRationalFunction(String output) {
        if (output == null || output.isEmpty()) {
            return null;
        }
        
        // First try to match output that includes the explicit "Parametric model checking result:" label
        Pattern pattern = Pattern.compile("Parametric model checking result:\\s*(.+?)\\s*(?:\\n|$)");
        Matcher matcher = pattern.matcher(output);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // If not found, try the generic "Result:" pattern
        pattern = Pattern.compile("Result:\\s*(.+?)\\s*(?:\\n|$)");
        matcher = pattern.matcher(output);
        
        if (matcher.find()) {
            String result = matcher.group(1).trim();
            // If it's not a simple numeric result, it's likely a rational function
            if (!result.matches("[0-9.Ee\\-]+")) {
                return result;
            }
        }
        
        // As a fallback, look for any expression with parameters
        pattern = Pattern.compile("(?:Simplified )?Expression: (.+?)\\s*(?:\\n|$)");
        matcher = pattern.matcher(output);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        System.err.println("No rational function found in PRISM output");
        return null;
    }
    
    /**
     * Checks if PRISM output indicates an error occurred
     * 
     * @param output The PRISM output to check
     * @return true if an error occurred, false otherwise
     */
    public static boolean hasError(String output) {
        if (output == null) {
            return true;
        }
        
        // Check for common PRISM error messages
        return output.contains("ERROR:") || 
               output.contains("Error:") || 
               output.contains("Exception") || 
               output.contains("failed") ||
               output.contains("FileNotFoundException") ||
               output.contains("Invalid") ||
               output.contains("Could not") ||
               (output.contains("exit") && output.contains("code") && !output.contains("code 0"));
    }
    
    /**
     * Extracts the error message from PRISM output
     * 
     * @param output The PRISM output to parse
     * @return The error message, or null if no error was found
     */
    public static String getErrorMessage(String output) {
        if (output == null || !hasError(output)) {
            return null;
        }
        
        // Try to find error message lines
        Pattern pattern = Pattern.compile("(?:ERROR|Error|Exception):?\\s*(.+)");
        Matcher matcher = pattern.matcher(output);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // If no specific error message found, return a generic message
        return "An error occurred in PRISM execution";
    }
    
    /**
     * Extracts all warnings from PRISM output
     * 
     * @param output The PRISM output to parse
     * @return Array of warning messages, or null if no warnings found
     */
    public static String[] getWarnings(String output) {
        if (output == null || output.isEmpty()) {
            return null;
        }
        
        // Find all warning lines in the output
        Pattern pattern = Pattern.compile("Warning:?\\s*(.+?)\\s*(?:\\n|$)");
        Matcher matcher = pattern.matcher(output);
        
        StringBuilder warnings = new StringBuilder();
        while (matcher.find()) {
            warnings.append(matcher.group(1).trim()).append("\n");
        }
        
        if (warnings.length() > 0) {
            return warnings.toString().split("\n");
        }
        
        return null;
    }
}