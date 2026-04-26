package verification;

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

	// Works for both Storm ("States: \t4") and PRISM ("States:      27 (1 initial)")
	public static long getStates(String output) {
		if (output == null) return -1;
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("States:\\s+(\\d+)").matcher(output);
		return m.find() ? Long.parseLong(m.group(1)) : -1;
	}

	public static long getTransitions(String output) {
		if (output == null) return -1;
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("Transitions:\\s+(\\d+)").matcher(output);
		return m.find() ? Long.parseLong(m.group(1)) : -1;
	}
}