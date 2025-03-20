package verification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Model;
import utils.PrismOutputParser;

/**
 * API to invoke PRISM-games as an external process
 */
public class PrismGamesProcessAPI {
	
    private static final Logger logger = LoggerFactory.getLogger(PrismGamesProcessAPI.class);
    
    /**
     * Run PRISM-games as an external process to verify a game model
     * 
     * @param model The game model to verify
     * @param property The property to check
     * @param prismGamesInstallLocation The path to the PRISM-games executable
     * @return The result of model checking
     * @throws IOException If there is an error creating the temporary property file
     */
    public static double run(Model model, String property, String prismGamesInstallLocation) throws IOException {
        // Create a temporary property file
        String propFilePath = createTemporaryPropertyFile(property);
        
        // Run PRISM-games with property file
        String command = prismGamesInstallLocation + " \"" + model.getVerificationFilePath() + "\"" + " " + propFilePath;
        logger.info("Executing PRISM-games command: " + command);
        
        String output = OSCommandExecutor.executeCommand(command);
        logger.info("PRISM-games output:\n" + output);
        
        if (PrismOutputParser.hasError(output)) {
            String errorMessage = PrismOutputParser.getErrorMessage(output);
            logger.error("PRISM-games execution failed: " + errorMessage);
            throw new RuntimeException("PRISM-games execution failed: " + errorMessage);
        }
        
        Double result = PrismOutputParser.getResult(output);
        
        if (result == null) {
            logger.error("Failed to parse PRISM-games result from output");
            throw new RuntimeException("Failed to parse PRISM-games result from output");
        }
        
        // Clean up the temporary property file
        cleanupTemporaryFile(propFilePath);
        
        return result;
    }
    
    /**
     * Run PRISM-games as an external process with additional parameters
     * 
     * @param model The game model to verify
     * @param property The property to check
     * @param prismGamesInstallLocation The path to the PRISM-games executable
     * @param additionalArgs Additional command-line arguments for PRISM-games
     * @return The result of model checking
     * @throws IOException If there is an error creating the temporary property file
     */
    public static double run(Model model, String property, String prismGamesInstallLocation, List<String> additionalArgs) throws IOException {
        // Create a temporary property file
        String propFilePath = createTemporaryPropertyFile(property);
        
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(prismGamesInstallLocation)
                      .append(" ")
                      .append(model.getVerificationFilePath())
                      .append(" ")
                      .append(propFilePath);
        
        // Add any additional arguments
        for (String arg : additionalArgs) {
            commandBuilder.append(" ").append(arg);
        }
        
        String command = commandBuilder.toString();
        logger.info("Executing PRISM-games command: " + command);
        
        String output = OSCommandExecutor.executeCommand(command);
        logger.info("PRISM-games output:\n" + output);
        
        if (PrismOutputParser.hasError(output)) {
            String errorMessage = PrismOutputParser.getErrorMessage(output);
            logger.error("PRISM-games execution failed: " + errorMessage);
            throw new RuntimeException("PRISM-games execution failed: " + errorMessage);
        }
        
        Double result = PrismOutputParser.getResult(output);
        
        if (result == null) {
            logger.error("Failed to parse PRISM-games result from output");
            throw new RuntimeException("Failed to parse PRISM-games result from output");
        }
        
        // Clean up the temporary property file
        cleanupTemporaryFile(propFilePath);
        
        return result;
    }
    
    /**
     * Creates a temporary property file with the given property
     * 
     * @param property The property text to write to the file
     * @return The path to the created temporary file
     * @throws IOException If there is an error creating the file
     */
    private static String createTemporaryPropertyFile(String property) throws IOException {
        // Create a temporary file for the property
        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = "property_" + System.currentTimeMillis() + ".props";
        String filePath = Paths.get(tempDir, filename).toString();
        
        // Ensure the property ends with a newline
        if (!property.endsWith("\n")) {
            property += "\n";
        }
        
        // Write the property to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(property);
        }
        
        logger.info("Created temporary property file: " + filePath);
        return filePath;
    }
    
    /**
     * Cleans up a temporary file
     * 
     * @param filePath The path to the file to clean up
     */
    private static void cleanupTemporaryFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists() && file.delete()) {
                logger.info("Deleted temporary file: " + filePath);
            } else {
                logger.warn("Failed to delete temporary file: " + filePath);
            }
        } catch (Exception e) {
            logger.warn("Error cleaning up temporary file: " + e.getMessage());
        }
    }
}