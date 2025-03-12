package verification_engine.storm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import headless.StormOutputParser;
import model.persistent_objects.Model;

public class StormAPI {
	
	public static double run(Model model, String propFile, String stormInstallLocation) {
		String command = stormInstallLocation + " --prism " + model.getFilePath()  + " --prop \"" + propFile + "\"" + " -pc";
		System.out.println(command);
		String output = OSCommandExecutor.executeCommand(command);
		System.out.println(output);
		Double result = StormOutputParser.getDResult(output);
		return result;
	}
	
	public static String runPars(Model model, String propFile, String stormInstallLocation) {
        // Parse and properly format the property based on its type
     //   String formattedProp = formatPropertyString(propFile);
        
        // Build and execute the command
        String command = stormInstallLocation + " --mode solutionfunction --prism " + model.getFilePath() + " --prop \"" + propFile + "\"";
        System.out.println("Executing command: " + command);
        
        String output = OSCommandExecutor.executeCommand(command);
        String result = StormOutputParser.getSResult(output);
        System.out.println("Command output: " + output);
        
        return result;
    }
    
    /**
     * Formats a property string to be compatible with Storm's command line syntax.
     * Detects property type (probability, reward, etc.) and formats accordingly.
     * 
     * @param propertyString The property string to format
     * @return Properly formatted property string
     */
    private static String formatPropertyString(String propertyString) {
        // Trim any whitespace
        String trimmedProp = propertyString.trim();
        
        // Check for reward property format: R{"name"}=? or R{name}=?
        Pattern rewardPattern = Pattern.compile("R\\s*\\{\\s*[\"']?([\\w]+)[\"']?\\s*\\}\\s*=\\s*\\?");
        Matcher rewardMatcher = rewardPattern.matcher(trimmedProp);
        
        if (rewardMatcher.find()) {
            // This is a reward property, extract reward name
            String rewardName = rewardMatcher.group(1);
            
            // Extract the formula part (everything after the =?)
            int formulaStart = trimmedProp.indexOf("=?") + 2;
            String formula = "";
            if (formulaStart < trimmedProp.length()) {
                formula = trimmedProp.substring(formulaStart).trim();
            }
            
            // Reconstruct with proper quoting for the reward name
            return "R{\"" + rewardName + "\"}=? " + formula;
        }
        
        // Check for simple reward property format: R{name} or R{"name"}
        Pattern simpleRewardPattern = Pattern.compile("R\\s*\\{\\s*[\"']?([\\w]+)[\"']?\\s*\\}");
        Matcher simpleRewardMatcher = simpleRewardPattern.matcher(trimmedProp);
        
        if (simpleRewardMatcher.find()) {
            // This is a simple reward property, extract reward name
            String rewardName = simpleRewardMatcher.group(1);
            
            // Extract anything after the reward name
            int afterReward = simpleRewardMatcher.end();
            String restOfProperty = "";
            if (afterReward < trimmedProp.length()) {
                restOfProperty = trimmedProp.substring(afterReward).trim();
            }
            
            // Reconstruct with proper quoting
            return "R{\"" + rewardName + "\"}" + restOfProperty;
        }
        
        // For normal properties, just return as is (with escaping if needed)
        return trimmedProp.replace("\"", "\\\"");
    }
	
}
