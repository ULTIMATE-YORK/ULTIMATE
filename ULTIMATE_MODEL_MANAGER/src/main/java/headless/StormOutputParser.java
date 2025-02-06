package headless;

public class StormOutputParser {
	
	// to get results from storm
	public static Double getDResult(String output) {
        // Split the output into lines
        String[] lines = output.split("\\r?\\n");
        // Look for the line containing the result
        for (String line : lines) {
            if (line.contains("Result (for initial states):")) {
                // Extract the part after the colon and trim any whitespace
                String resultStr = line.substring(line.indexOf("Result (for initial states):") 
                                    + "Result (for initial states):".length()).trim();
                try {
                    // Parse and return the result as a Double
                    return Double.parseDouble(resultStr);
                } catch (NumberFormatException e) {
                    // If parsing fails, you might choose to handle the error differently
                    e.printStackTrace();
                    return null;
                }
            }
        }
        // Return null if the result line wasn't found
        return null;
	}
	
	// to get results from storm-pars
	public static String getSResult(String output) {
        // Split the output into lines
        String[] lines = output.split("\\r?\\n");
        // Look for the line containing the result
        for (String line : lines) {
            if (line.contains("Result (initial states):")) {
                // Extract and return the substring after the colon, trimmed of whitespace
                return line.substring(line.indexOf("Result (initial states):") 
                        + "Result (initial states):".length()).trim();
            }
        }
        // Return null if the result line wasn't found
        return null;
	}
}