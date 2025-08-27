package verification;

import project.Project;
import sharedContext.SharedContext;
import utils.FileUtils;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Model;
import parameters.DependencyParameter;
import parameters.ExternalParameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;

public class NPMCVerification {

    private static final Logger logger = LoggerFactory.getLogger(NPMCVerification.class);

    // static class Model {
    // // wrapper around the model
    // private String modelId;
    // private HashMap<String, ExternalParameter> parameters;

    // public Model(String modelId) {
    // this.modelId = modelId;
    // this.parameters = new HashMap<>();
    // }

    // public void setDependencyParameter(String paramName, String value) {
    // try {
    // this.parameters.get(paramName).setValue(value);
    // } catch (Exception e) {
    // throw new RuntimeException("Error setting dependency parameter value.");
    // }
    // logger.info(" → Setting parameter " + paramName + " = " + value + " for model
    // " + modelId);
    // }

    // public HashMap<String, String> getDependencyParameters() {
    // return parameters;
    // }

    // public String getModelId() {
    // return modelId;
    // }

    // @Override
    // public String toString() {
    // return modelId;
    // }

    // @Override
    // public boolean equals(Object obj) {
    // if (obj instanceof Model) {
    // return this.modelId.equals(((Model) obj).modelId);
    // }
    // return false;
    // }

    // @Override
    // public int hashCode() {
    // return modelId.hashCode();
    // }
    // }

    private Map<String, Model> modelMap;
    private Map<Model, List<Model>> sccMap;
    private ArrayList<Model> originalModels;
    private boolean usePythonSolver = false;
    private String pythonSolverPath = "ULTIMATE_Numerical_Solver/ULTIMATE_numerical_solver.py";

    public NPMCVerification(ArrayList<Model> models) {
        this.originalModels = models;
        this.modelMap = new HashMap<>();
        initializeFromModels(models);
    }

    public void setPythonSolverPath(String path) {
        this.pythonSolverPath = path;
    }

    public void setModelsBasePath(String path) {
    }

    private void initializeFromModels(ArrayList<Model> models) {
        logger.info("Initializing verification from models...");

        // Create VerificationModel objects
        for (Model model : models) {
            // Model vm = new Model(model.getModelId());
            modelMap.put(model.getModelId(), model);
        }

        // Compute SCCs using DependencyGraph
        DependencyGraph depGraph = new DependencyGraph(models);
        List<Set<String>> sccs = depGraph.getSCC();

        // Convert SCCs to VerificationModel format
        sccMap = new HashMap<>();
        for (Set<String> scc : sccs) {
            List<Model> sccModels = new ArrayList<>();
            for (String modelId : scc) {
                sccModels.add(modelMap.get(modelId));
            }
            for (Model vm : sccModels) {
                sccMap.put(vm, sccModels);
            }
        }

        logger.info("Found SCCs: " + sccs);
    }

    public String verify(String startModelId, String property) throws IOException {
        Model startModel = modelMap.get(startModelId);
        return verifyModel(startModel, property);
    }

    private String verifyModel(Model verificationModel, String property) throws IOException {
        logger.info(
                "\n=== Starting verification for model " + verificationModel + " with property " + property + " ===");

        List<Model> currentSCC = sccMap.get(verificationModel);
        logger.info("Current SCC: " + currentSCC);

        // Check if we need to use Python solver for this SCC
        if (!usePythonSolver) {
            for (Model model : currentSCC) {
                logger.info("\nProcessing model: " + model);
                List<DependencyParameter> dependencies = getDependencyParams(model.getModelId());
                logger.info("Dependencies found: " + dependencies);

                for (DependencyParameter dep : dependencies) {
                    Model targetModel = modelMap.get(dep.getSourceModel().getModelId());
                    List<Model> targetSCC = sccMap.get(targetModel);

                    if (!targetSCC.equals(currentSCC)) {
                        logger.info("  Processing dependency: " + dep);
                        logger.info("  Target model " + targetModel + " is in different SCC: " + targetSCC);
                        String result = verifyModel(targetModel, dep.getDefinition());
                        model.setDependencyParameter(dep.getNameInModel(), result);
                    } else {
                        logger.info("  Skipping dependency: " + dep + " (same SCC)");
                    }
                }
            }

            if (currentSCC.size() > 1) {
                logger.info("\nResolving SCC for models: " + currentSCC);
                try {
                    resolveSCC(currentSCC);
                } catch (Exception e) {
                    logger.error("Failed to resolve SCC using parametric model checking: " + e.getMessage());
                    logger.info("Switching to Python numerical solver...");
                    usePythonSolver = true;
                    // Reset any partial parameter values that might have been calculated
                    for (Model model : currentSCC) {
                        model.resetDependencyParameters();
                    }
                    // Call Python solver
                    resolveSCCWithPythonSolver(currentSCC);
                }
            }
        } else {
            // If we've already switched to Python solver, use it directly for this SCC
            if (currentSCC.size() > 1) {
                logger.info("\nResolving SCC using Python solver for models: " + currentSCC);
                resolveSCCWithPythonSolver(currentSCC);
            }
        }

        String result = performPMC(verificationModel, property);
        logger.info("Final PMC result for " + verificationModel + ": " + result);
        // TODO : export model files with parameters set
        return result;
    }

    private void resolveSCCWithPythonSolver(List<Model> sccModels) {
        logger.info("Starting SCC resolution using Python solver for models: " + sccModels);

        try {
            // Prepare input for Python solver
            List<String> inputData = new ArrayList<>();
            Model startVerificationModel = sccModels.get(0);
            String startModelId = startVerificationModel.getModelId();
            Project project = SharedContext.getProject();
            String pmcPath = project.getStormInstall(); // Update as needed

            // Get the file path of the start model
            Model originalStartModel = getOriginalModel(startModelId);
            if (originalStartModel == null) {
                logger.error("Could not find original model for ID: " + startModelId);
                throw new RuntimeException("Could not find original model for ID: " + startModelId);
            }

            String modelFilePath = originalStartModel.getVerificationFilePath();
            if (modelFilePath == null || modelFilePath.isEmpty()) {
                logger.error("Model path is empty for model: " + startModelId);
                throw new RuntimeException("Model path is empty for model: " + startModelId);
            }

            for (Model model : sccModels) {
                Model originalModel = getOriginalModel(model.getModelId());
                if (originalModel == null) {
                    logger.warn("Warning: Could not find original model for ID: " + model.getModelId());
                    continue;
                }

                String currentModelFilePath = originalModel.getVerificationFilePath();
                if (currentModelFilePath == null || currentModelFilePath.isEmpty()) {
                    logger.warn("Warning: File path is empty for model: " + model.getModelId());
                    continue;
                }

                for (DependencyParameter dep : getDependencyParams(model.getModelId())) {
                    Model targetModel = modelMap.get(dep.getSourceModel().getModelId());

                    if (sccModels.contains(targetModel)) {
                        Model originalTargetModel = getOriginalModel(targetModel.getModelId());
                        if (originalTargetModel == null) {
                            logger.warn("Warning: Could not find original target model for ID: "
                                    + targetModel.getModelId());
                            continue;
                        }

                        String targetModelFilePath = originalTargetModel.getVerificationFilePath();
                        if (targetModelFilePath == null || targetModelFilePath.isEmpty()) {
                            logger.warn("Warning: File path is empty for target model: " + targetModel.getModelId());
                            continue;
                        }

                        // Format: "dependent_model, source_model, property, variable_name"
                        String inputLine = currentModelFilePath + ", " +
                                targetModelFilePath + ", " +
                                dep.getDefinition() + ", " +
                                dep.getNameInModel();
                        inputData.add(inputLine);
                    }
                }
            }

            if (inputData.isEmpty()) {
                logger.info("No dependencies found for SCC models. Skipping Python solver.");
                return;
            }

            // Build command for Python solver
            List<String> command = new ArrayList<>();
            String pythonPath = project.getPythonInstall();
            command.add(pythonPath);
            command.add(pythonSolverPath);
            command.add("--path");
            command.add(pmcPath);
            command.add("--mc");
            command.add("storm");
            command.add("--model");
            command.add(modelFilePath);
            command.add("--input");
            command.addAll(inputData);
            // command.add(" -pc");

            logCommandWithLineBreaks(logger, command);

            logger.info("Executing Python solver with command: ");
            for (String cmd : command) {
                logger.info(cmd + " ");
            }
            logger.info("Input data:");
            for (String input : inputData) {
                logger.info(input);
            }

            // Execute Python solver
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            long startTime = System.currentTimeMillis();
            Process process = processBuilder.start();
            long endTime = System.currentTimeMillis();

            // Read output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            Map<String, String> results = new HashMap<>();

            while ((line = reader.readLine()) != null) {
                logger.info(line);

                // Parse results from Python output
                if (line.startsWith("Optimal parameters:")) {
                    // Parse parameters from line like "Optimal parameters: {'param1': 0.123,
                    // 'param2': 0.456}"
                    String paramsStr = line.substring(line.indexOf('{') + 1, line.lastIndexOf('}'));
                    String[] params = paramsStr.split(", ");

                    for (String param : params) {
                        String[] keyValue = param.split(":");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim().replace("'", "");
                            String value = keyValue[1].trim();
                            results.put(key, value);
                        }
                    }
                }
            }

            int exitCode = process.waitFor();
            logger.info("Python solver exited with code: " + exitCode + " Time elapsed: " + (endTime - startTime));

            if (exitCode != 0) {
                logger.error("Python solver failed with exit code " + exitCode);
                throw new RuntimeException("Python solver failed with exit code " + exitCode);
            }

            // Set calculated parameters to models
            if (!results.isEmpty()) {
                for (Map.Entry<String, String> entry : results.entrySet()) {
                    String paramName = entry.getKey();
                    String paramValue = entry.getValue();

                    // Find which model this parameter belongs to
                    for (Model model : sccModels) {
                        for (DependencyParameter dep : getDependencyParams(model.getModelId())) {
                            if (dep.getNameInModel().equals(paramName)) {
                                model.setDependencyParameter(dep.getNameInModel(), paramValue);
                                break;
                            }
                        }
                    }
                }
            } else {
                logger.error("Failed to get results from Python solver");
                throw new RuntimeException("Failed to get results from Python solver");
            }

        } catch (IOException e) {
            logger.error("IOException when executing Python solver: " + e.getMessage());
            throw new RuntimeException("Failed to execute Python solver: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error("Process interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupted state
            throw new RuntimeException("Python solver process interrupted: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during Python solver execution: " + e.getMessage());
            // e.printStackTrace();
            throw new RuntimeException("Python solver failed: " + e.getMessage(), e);
        }
    }

    private void resolveSCC(List<Model> sccModels) {
        logger.info("Starting SCC resolution for models: " + sccModels);

        // Store equations and their variables
        List<String> equations = new ArrayList<>();
        Set<String> variableSet = new HashSet<>();
        Map<String, String> equationMap = new HashMap<>();

        // Collect equations and variables
        for (Model model : sccModels) {
            logger.info("\nGetting dependencies for model: " + model);
            for (DependencyParameter dep : getDependencyParams(model.getModelId())) {
                Model targetModel = modelMap.get(dep.getSourceModel().getModelId());

                if (sccModels.contains(targetModel)) {
                    String rationalFunction = getRationalFunction(targetModel, dep.getDefinition(), null);

                    // Check if rational function is null or empty
                    if (rationalFunction == null || rationalFunction.trim().isEmpty()) {
                        throw new RuntimeException("Failed to get rational function for " + dep.getNameInModel() +
                                " in model " + model.getModelId() + "(" + dep.getNameInModel() + ")");
                    }

                    String equation = dep.getNameInModel() + " = " + rationalFunction;
                    logger.info("  Adding equation: " + equation);

                    // Transform equation to standard form f(x) = 0
                    String transformedEq = transformEquation(equation);
                    equations.add(transformedEq);
                    equationMap.put(dep.getNameInModel(), transformedEq);
                    variableSet.add(dep.getNameInModel());
                }
            }
        }

        // Convert variables set to array for ordering
        String[] variables = variableSet.toArray(new String[0]);
        logger.info("\nSolving equation system:");
        logger.info("Variables: " + Arrays.toString(variables));
        logger.info("Equations: " + equations);

        // Initialize arguments with starting values
        Argument[] args = new Argument[variables.length];
        for (int i = 0; i < variables.length; i++) {
            args[i] = new Argument(variables[i] + " = 1");
        }

        // Solve the system iteratively
        boolean converged = false;
        int maxIterations = 100;
        double tolerance = 0.0001;
        Map<String, String> solutions = new HashMap<>();

        for (int iteration = 0; iteration < maxIterations && !converged; iteration++) {
            logger.info("\nIteration " + (iteration + 1) + ":");
            boolean iterationConverged = true;

            // Solve for each variable
            for (int i = 0; i < variables.length; i++) {
                String variable = variables[i];
                String equation = equationMap.get(variable);

                // Create expression to solve for current variable
                String solveExpr = "solve(" + equation + ", " + variable + ", 0, 1)";
                Expression e = new Expression(solveExpr, args);

                double newValue = e.calculate();
                if (Double.isNaN(newValue)) {
                    logger.warn("  Failed to solve for " + variable);
                    continue;
                }

                // Check convergence for this variable
                double oldValue = args[i].getArgumentValue();
                double diff = Math.abs(oldValue - newValue);
                if (diff > tolerance) {
                    iterationConverged = false;
                }

                // Update value
                args[i].setArgumentValue(newValue);
                solutions.put(variable, String.valueOf(newValue));
                logger.info("  " + variable + " = " + newValue);
            }

            converged = iterationConverged;
        }

        if (!converged) {
            logger.warn("\nWarning: Maximum iterations reached without convergence");
        }

        // Set the solved values to the models
        logger.info("\nFinal solutions:");
        for (Map.Entry<String, String> solution : solutions.entrySet()) {
            logger.info(solution.getKey() + " = " + solution.getValue());
            for (Model model : sccModels) {
                model.setDependencyParameter(solution.getKey(), solution.getValue());
            }
        }
    }

    private String transformEquation(String equation) {
        // Split equation into left and right sides
        String[] sides = equation.split("\\s*=\\s*");
        if (sides.length != 2) {
            logger.error("Invalid equation format: " + equation);
            throw new IllegalArgumentException("Invalid equation format: " + equation);
        }

        // Move everything to left side (subtract right side)
        return "(" + sides[0] + ")-(" + sides[1] + ")";
    }

    private List<DependencyParameter> getDependencyParams(String modelId) {
        for (Model model : originalModels) {
            if (model.getModelId().equals(modelId)) {
                return model.getDependencyParameters();
            }
        }
        return new ArrayList<>();
    }

    private String getRationalFunction(Model model, String property, List<String> paramNames) {
        logger.info("Performing parametric MC for " + model + " with property " + property);

        Model originalModel = getOriginalModel(model.getModelId());
        if (originalModel == null) {
            logger.error("Model not found: " + model.getModelId());
            throw new IllegalArgumentException("Model not found: " + model.getModelId());
        }

        try {
            StormAPI sAPI = new StormAPI();
            String equationStr = sAPI.runPars(originalModel, property);
            logger.info("Received equation: " + equationStr);
            return equationStr;
        } catch (Exception e) {
            logger.error("Error in parametric model checking: " + e.getMessage());
            logger.info("Will try Python numerical solver instead");
            return null;
        }
    }

    private String performPMC(Model model, String property) throws FileNotFoundException {
        // System.out.println("Performing PMC for " + model + " with property " +
        // property);
        logger.info("Performing PMC for " + model + " with property " + property);
        Model originalModel = getOriginalModel(model.getModelId());
        if (originalModel == null) {
            logger.error("Model not found: " + model.getModelId());
            throw new IllegalArgumentException("Model not found: " + model.getModelId());
        }
        // Don't know how this is meant to work. I think it's a substitute for an
        // if-statement??? -- BDH
        try {
            FileUtils.writeParametersToFile(originalModel.getVerificationFilePath(), originalModel.getHashParameters());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Check if the model is a PRISM-games model
        boolean isPrismGamesModel = false;
        try {
            String modelFilePath = originalModel.getVerificationFilePath(); // this
            if (modelFilePath != null && !modelFilePath.isEmpty()) {
                // Read the content of the model file to check for game model keywords
                String modelContent = FileUtils.readFileAsString(modelFilePath);

                // Check if model content contains game model identifiers anywhere in the file
                // We don't limit to just the first line as the model type might be after
                // comments
                if (modelContent != null &&
                        (modelContent.contains("smg") ||
                                modelContent.contains("tsg") ||
                                modelContent.contains("csg") ||
                                modelContent.contains("tptg"))) {
                    isPrismGamesModel = true;
                    logger.info("Detected PRISM-games model type: file contains game model identifier");
                }
            }
        } catch (IOException e) {
            logger.warn("Could not read model file to check for game model type: " + e.getMessage());
        }

        // If it's a PRISM-games model, use PrismGamesProcessAPI
        if (isPrismGamesModel) {
            logger.info("Detected PRISM-games model type. Using PrismGamesProcessAPI...");
            try {
                SharedContext sharedContext = SharedContext.getContext();
                Project project = SharedContext.getProject();
                String prismGamesPath = project.getPrismGamesInstall();

                // Check if prismGamesPath is valid
                if (prismGamesPath == null || prismGamesPath.isEmpty()) {
                    logger.warn("PRISM-games installation path is not configured. Using default location.");
                    // Try to use a default location if not configured
                    prismGamesPath = project.getPrismInstall().replace("prism", "prism-games");
                }

                logger.info("Using PRISM-games executable path: " + prismGamesPath);

                // Always export strategy for PRISM-games models
                String modelName = getModelName(originalModel.getVerificationFilePath());
                String propertyName = getPropertyName(property);

                // Create a directory for strategies if it doesn't exist
                String strategyDir = "strategies";
                new File(strategyDir).mkdirs();

                String strategyFilePath = strategyDir + "/" + modelName + "_" + propertyName + ".dot";
                logger.info("Exporting strategy to: " + strategyFilePath);

                // Use property index 1 if not specified in the property string
                int propertyIndex = getPropertyIndex(property);

                double prismResult = PrismGamesProcessAPI.run(originalModel, property,
                        prismGamesPath);
                return String.valueOf(prismResult);
            } catch (IOException prismGamesException) {
                logger.error("Error running PrismGamesProcessAPI: " + prismGamesException.getMessage());
                // Fall back to other methods if PrismGames fails
            }
        }

        // First try with Storm
        try {
            StormAPI sAPI = new StormAPI();
            double stormResult = sAPI.run(originalModel, property);
            return String.valueOf(stormResult);
        } catch (Exception stormException) {
            // Extract just the error message without stack trace
            logger.error("Error running Storm: " + stormException.getMessage());
            // throw new Exception();
        }

        // Try with Prism as fallback
        logger.info("Trying  fallback with PRISM...");
        try {
            Project project = SharedContext.getProject();
            String prismPath = project.getPrismInstall();
            double prismResult = PrismProcessAPI.run(originalModel, property, prismPath);
            return String.valueOf(prismResult);
        } catch (IOException prismProcessException) {
            logger.error("Error running PRISM Process API: " + prismProcessException.getMessage());
            // Only throw if all attempts fail
            throw new RuntimeException("All model checking methods failed for model " + model.getModelId() +
                    ": " + prismProcessException.getMessage());
        }
    }

    private Model getOriginalModel(String modelId) {
        for (Model model : originalModels) {
            if (model.getModelId().equals(modelId)) {
                return model;
            }
        }
        return null;
    }

    /**
     * Extracts a model name from its file path for use in strategy export filename
     * 
     * @param modelFilePath The full path to the model file
     * @return A simplified model name (filename without extension)
     */
    private String getModelName(String modelFilePath) {
        if (modelFilePath == null || modelFilePath.isEmpty()) {
            return "model";
        }

        // Extract just the filename
        String fileName = new File(modelFilePath).getName();

        // Remove file extension
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }

        return fileName;
    }

    /**
     * Creates a simplified property name from a property string for use in strategy
     * export filename
     * 
     * @param property The property string
     * @return A simplified property name
     */
    private String getPropertyName(String property) {
        if (property == null || property.isEmpty()) {
            return "prop";
        }

        // Create a safe filename by replacing special characters
        String safeProperty = property.replaceAll("[^a-zA-Z0-9]", "_");

        // Limit length for filenames
        if (safeProperty.length() > 20) {
            safeProperty = safeProperty.substring(0, 20);
        }

        return safeProperty;
    }

    /**
     * Attempts to extract a property index from the property string
     * 
     * @param property The property string
     * @return The extracted property index, or 1 if not found
     */
    private int getPropertyIndex(String property) {
        // Default property index
        int propertyIndex = 1;

        // Check if property contains a specific index pattern like "P4:" or similar
        if (property != null && !property.isEmpty()) {
            // Look for patterns like "P1:", "P2:", etc.
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("P(\\d+):");
            java.util.regex.Matcher matcher = pattern.matcher(property);

            if (matcher.find()) {
                try {
                    propertyIndex = Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    // Ignore parsing errors and use default
                }
            }
        }

        return propertyIndex;
    }

    private void logCommandWithLineBreaks(Logger logger, List<String> command) {
        StringBuilder fullCommand = new StringBuilder();
        for (int i = 0; i < command.size(); i++) {
            logger.info(command.get(i) + (i < command.size() - 1 ? " \\" : ""));
            fullCommand.append(command.get(i)).append(" ");
        }
        logger.info("Full command: " + fullCommand.toString().trim());
    }
}
