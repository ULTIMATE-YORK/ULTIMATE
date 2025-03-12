package verification_engine.prism;

import model.persistent_objects.Model;
import utils.OSCommandExecutor;
import utils.PrismOutputParser;
import utils.ModelUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API to invoke PRISM as an external process
 */
public class PrismProcessAPI {
    
    /**
     * Run PRISM as an external process to verify a model
     * 
     * @param model The model to verify
     * @param property The property to check
     * @param prismInstallLocation The path to the PRISM executable
     * @return The result of model checking
     * @throws IOException If there is an error creating the temporary property file
     */
    public static double run(Model model, String property, String prismInstallLocation) throws IOException {
        // Create a temporary property file
        String propFilePath = createTemporaryPropertyFile(property);
        
        // Run PRISM with property file
        String command = prismInstallLocation + " " + model.getFilePath() + " " + propFilePath;
        System.out.println("Executing PRISM command: " + command);
        
        String output = OSCommandExecutor.executeCommand(command);
        System.out.println("PRISM output:\n" + output);
        
        if (PrismOutputParser.hasError(output)) {
            String errorMessage = PrismOutputParser.getErrorMessage(output);
            throw new RuntimeException("PRISM execution failed: " + errorMessage);
        }
        
        Double result = PrismOutputParser.getResult(output);
        
        if (result == null) {
            throw new RuntimeException("Failed to parse PRISM result from output");
        }
        
        // Clean up the temporary property file
        cleanupTemporaryFile(propFilePath);
        
        return result;
    }
    
    /**
     * Run PRISM as an external process with additional parameters
     * 
     * @param model The model to verify
     * @param property The property to check
     * @param prismInstallLocation The path to the PRISM executable
     * @param additionalArgs Additional command-line arguments for PRISM
     * @return The result of model checking
     * @throws IOException If there is an error creating the temporary property file
     */
    public static double run(Model model, String property, String prismInstallLocation, List<String> additionalArgs) throws IOException {
        // Create a temporary property file
        String propFilePath = createTemporaryPropertyFile(property);
        
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(prismInstallLocation)
                      .append(" ")
                      .append(model.getFilePath())
                      .append(" ")
                      .append(propFilePath);
        
        // Add any additional arguments
        for (String arg : additionalArgs) {
            commandBuilder.append(" ").append(arg);
        }
        
        String command = commandBuilder.toString();
        System.out.println("Executing PRISM command: " + command);
        
        String output = OSCommandExecutor.executeCommand(command);
        System.out.println("PRISM output:\n" + output);
        
        if (PrismOutputParser.hasError(output)) {
            String errorMessage = PrismOutputParser.getErrorMessage(output);
            throw new RuntimeException("PRISM execution failed: " + errorMessage);
        }
        
        Double result = PrismOutputParser.getResult(output);
        
        if (result == null) {
            throw new RuntimeException("Failed to parse PRISM result from output");
        }
        
        // Clean up the temporary property file
        cleanupTemporaryFile(propFilePath);
        
        return result;
    }
    
    /**
     * Run PRISM with constant values directly specified on the command line
     * 
     * @param model The model to verify
     * @param property The property to check
     * @param prismInstallLocation The path to the PRISM executable
     * @param constants Map of constant names to values
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
     * @param model The model to verify
     * @param property The property to check
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
        if (model.getUndefinedParameters() != null && !model.getUndefinedParameters().isEmpty()) {
            for (int i = 0; i < model.getUndefinedParameters().size(); i++) {
                if (i > 0) {
                    paramsBuilder.append(",");
                }
                paramsBuilder.append(model.getUndefinedParameters().get(i).getName());
            }
            args.add("-paramnames");
            args.add(paramsBuilder.toString());
        }
        
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(prismInstallLocation)
                      .append(" ")
                      .append(model.getFilePath())
                      .append(" ")
                      .append(propFilePath);
        
        // Add parametric arguments
        for (String arg : args) {
            commandBuilder.append(" ").append(arg);
        }
        
        String command = commandBuilder.toString();
        System.out.println("Executing parametric PRISM command: " + command);
        
        String output = OSCommandExecutor.executeCommand(command);
        System.out.println("Parametric PRISM output:\n" + output);
        
        if (PrismOutputParser.hasError(output)) {
            String errorMessage = PrismOutputParser.getErrorMessage(output);
            System.err.println("PRISM parametric execution error: " + errorMessage);
            return null;
        }
        
        String rationalFunction = PrismOutputParser.getRationalFunction(output);
        
        if (rationalFunction == null || rationalFunction.isEmpty()) {
            System.err.println("Failed to parse rational function from PRISM output");
            return null;
        }
        
        // Clean up the temporary property file
        cleanupTemporaryFile(propFilePath);
        
        return rationalFunction;
    }
    
    /**
     * Run PRISM with a model file that has been updated with specific parameter values
     * 
     * @param model The model to verify
     * @param property The property to check
     * @param prismInstallLocation The path to the PRISM executable
     * @param parameters Map of parameter names to values
     * @return The result of model checking
     * @throws IOException If there is an error creating the temporary property file
     */
    public static double runWithUpdatedModel(Model model, String property, String prismInstallLocation, 
                                           Map<String, Double> parameters) throws IOException {
        // Convert Map to HashMap as required by ModelUtils
        HashMap<String, Double> parametersMap = new HashMap<>(parameters);
        
        // Update the model file with the parameter values
        ModelUtils.updateModelFileResults(model, parametersMap);
        
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
        
        System.out.println("Created temporary property file: " + filePath);
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
                System.out.println("Deleted temporary file: " + filePath);
            } else {
                System.err.println("Failed to delete temporary file: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("Error cleaning up temporary file: " + e.getMessage());
        }
    }
}