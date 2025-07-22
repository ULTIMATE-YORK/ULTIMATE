package verification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Model;
import utils.FileUtils;
import utils.PrismOutputParser;

import parameters.InternalParameter;

/**
 * API to invoke PRISM as an external process
 */
public class PrismProcessAPI {

    private static final Logger logger = LoggerFactory.getLogger(PrismProcessAPI.class);

    /**
     * Run PRISM as an external process to verify a model
     * 
     * @param model                The model to verify
     * @param property             The property to check
     * @param prismInstallLocation The path to the PRISM executable
     * @return The result of model checking
     * @throws IOException If there is an error creating the temporary property file
     */
    public static double run(Model model, String property, String prismInstallLocation) throws IOException {
        // Create a temporary property file
        String propFilePath = createTemporaryPropertyFile(property);

        // Run PRISM with property file
        String command = prismInstallLocation + " \"" + model.getVerificationFilePath() + "\"" + " " + propFilePath;
        logger.info("Executing PRISM command: " + command);

        String output = OSCommandExecutor.executeCommand(command);
        logger.info("PRISM output:\n" + output);

        if (PrismOutputParser.hasError(output)) {
            String errorMessage = PrismOutputParser.getErrorMessage(output);
            logger.error("PRISM execution failed: " + errorMessage);
            throw new RuntimeException("PRISM execution failed: " + errorMessage);
        }

        Double result = PrismOutputParser.getResult(output);

        if (result == null) {
            logger.error("Failed to parse PRISM result from output");
            throw new RuntimeException("Failed to parse PRISM result from output");
        }

        // Clean up the temporary property file
        //cleanupTemporaryFile(propFilePath);

        return result;
    }

    //TODO: make into actual temporary files

    /**
     * Run PRISM as an external process with additional parameters
     * 
     * @param model                The model to verify
     * @param property             The property to check
     * @param prismInstallLocation The path to the PRISM executable
     * @param additionalArgs       Additional command-line arguments for PRISM
     * @return The result of model checking
     * @throws IOException If there is an error creating the temporary property file
     */
    public static double run(Model model, String property, String prismInstallLocation, List<String> additionalArgs)
            throws IOException {
        // Create a temporary property file
        String propFilePath = createTemporaryPropertyFile(property);

        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(prismInstallLocation)
                .append(" ")
                .append(model.getVerificationFilePath())
                .append(" ")
                .append(propFilePath);

        // Add any additional arguments
        for (String arg : additionalArgs) {
            commandBuilder.append(" ").append(arg);
        }

        String command = commandBuilder.toString();
        logger.info("Executing PRISM command: " + command);

        String output = OSCommandExecutor.executeCommand(command);
        logger.info("PRISM output:\n" + output);

        if (PrismOutputParser.hasError(output)) {
            String errorMessage = PrismOutputParser.getErrorMessage(output);
            logger.error("PRISM execution failed: " + errorMessage);
            throw new RuntimeException("PRISM execution failed: " + errorMessage);
        }

        Double result = PrismOutputParser.getResult(output);

        if (result == null) {
            logger.error("Failed to parse PRISM result from output");
            throw new RuntimeException("Failed to parse PRISM result from output");
        }

        // Clean up the temporary property file
        //cleanupTemporaryFile(propFilePath);

        return result;
    }

    /**
     * Run PRISM with constant values directly specified on the command line
     * 
     * @param model                The model to verify
     * @param property             The property to check
     * @param prismInstallLocation The path to the PRISM executable
     * @param constants            Map of constant names to values
     * @return The result of model checking
     * @throws IOException If there is an error creating the temporary property file
     */
    public static double runWithConstants(Model model, String property, String prismInstallLocation,
            Map<String, Double> constants) throws IOException {
        if (constants == null || constants.isEmpty()) {
            return run(model, property, prismInstallLocation);
        }

        // Build the constants string
        StringBuilder constBuilder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Double> entry : constants.entrySet()) {
            if (!first) {
                constBuilder.append(",");
            }
            constBuilder.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
            first = false;
        }

        List<String> args = new ArrayList<>();
        args.add("-const");
        args.add(constBuilder.toString());

        return run(model, property, prismInstallLocation, args);
    }

    /**
     * Run parametric model checking with PRISM
     * 
     * @param model                The model to verify
     * @param property             The property to check
     * @param prismInstallLocation The path to the PRISM executable
     * @return The rational function as a string
     * @throws IOException If there is an error creating the temporary property file
     */
    public static String runPars(Model model, String property, String prismInstallLocation) throws IOException {
        // Create a temporary property file
        String propFilePath = createTemporaryPropertyFile(property);

        List<String> args = new ArrayList<>();
        args.add("-param");

        // Get all undefined parameters from the model
        StringBuilder paramsBuilder = new StringBuilder();
        if (model.getUncategorisedParameters() != null && !model.getUncategorisedParameters().isEmpty()) {
            for (int i = 0; i < model.getUncategorisedParameters().size(); i++) {
                if (i > 0) {
                    paramsBuilder.append(",");
                }
                paramsBuilder.append(model.getUncategorisedParameters().get(i).getName());
            }
            args.add("-paramnames");
            args.add(paramsBuilder.toString());
        }

        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(prismInstallLocation)
                .append(" ")
                .append(model.getVerificationFilePath())
                .append(" ")
                .append(propFilePath);

        // Add parametric arguments
        for (String arg : args) {
            commandBuilder.append(" ").append(arg);
        }

        String command = commandBuilder.toString();
        logger.info("Executing parametric PRISM command: " + command);

        String output = OSCommandExecutor.executeCommand(command);
        logger.info("Parametric PRISM output:\n" + output);

        if (PrismOutputParser.hasError(output)) {
            String errorMessage = PrismOutputParser.getErrorMessage(output);
            logger.warn("PRISM parametric execution error: " + errorMessage);
            return null;
        }

        String rationalFunction = PrismOutputParser.getRationalFunction(output);

        if (rationalFunction == null || rationalFunction.isEmpty()) {
            logger.warn("Failed to parse rational function from PRISM output");
            return null;
        }

        // Clean up the temporary property file
        cleanupTemporaryFile(propFilePath);

        return rationalFunction;
    }

    /**
     * Run PRISM with a model file that has been updated with specific parameter
     * values
     * 
     * @param model                 The model to verify
     * @param property              The property to check
     * @param prismInstallLocation  The path to the PRISM executable
     * @param externalParametersMap Map of external parameter names to values
     * @return The result of model checking
     * @throws IOException If there is an error creating the temporary property file
     */
    public static double runWithUpdatedModel(Model model, String property, String prismInstallLocation,
            Map<String, Double> externalParametersMap, Map<String, InternalParameter> internalParametersMap)
            throws IOException {
        // Convert Map to HashMap as required by ModelUtils
        HashMap<String, Double> hashExternalParameters = new HashMap<>(externalParametersMap);
        HashMap<String, InternalParameter> hashInternalParameters = new HashMap<>(internalParametersMap);

        // Update the model file with the parameter values
        FileUtils.writeParametersToFile(model.getVerificationFilePath(), hashExternalParameters,
                hashInternalParameters);

        // Run verification on the updated model
        double result = run(model, property, prismInstallLocation);

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