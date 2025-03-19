package verification;

import project.Project;
import sharedContext.SharedContext;
import utils.FileUtils;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
//import org.mariuszgromada.math.mxparser.License;
//import org.mariuszgromada.math.mxparser.mXparser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Model;
import parameters.DependencyParameter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;

public class NPMCVerification {
   	
	private static final Logger logger = LoggerFactory.getLogger(NPMCVerification.class);
	
    static class VerificationModel {
        private String modelId;
        private HashMap<String, Double> parameters;
        
        public VerificationModel(String modelId) {
            this.modelId = modelId;
            this.parameters = new HashMap<>();
        }
        
        public void setParameter(String paramName, double value) {
            this.parameters.put(paramName, value);
            logger.info("    → Setting parameter " + paramName + " = " + value + " for model " + modelId);
        }
        
        public HashMap<String, Double> getParameters() {
            return parameters;
        }
        
        public String getModelId() {
            return modelId;
        }
        
        @Override
        public String toString() {
            return modelId;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof VerificationModel) {
                return this.modelId.equals(((VerificationModel) obj).modelId);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return modelId.hashCode();
        }
    }

    private Map<String, VerificationModel> modelMap;
    private Map<VerificationModel, List<VerificationModel>> sccMap;
    private ArrayList<Model> originalModels;
    private boolean usePythonSolver = false;
    private String pythonSolverPath = "ULTIMATE_Numerical_Solver/ULTIMATE_numerical_solver.py";
    public NPMCVerification(ArrayList<Model> models) {
        //License.iConfirmNonCommercialUse("ultimate,");  // Add this line to confirm license for math lib
        //mXparser.consolePrintln(false);  // Disable mXparser console output of math lib
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
            VerificationModel vm = new VerificationModel(model.getModelId());
            modelMap.put(model.getModelId(), vm);
        }

        // Compute SCCs using DependencyGraph
        DependencyGraph depGraph = new DependencyGraph(models);
        List<Set<String>> sccs = depGraph.getSCC();
        
        // Convert SCCs to VerificationModel format
        sccMap = new HashMap<>();
        for (Set<String> scc : sccs) {
            List<VerificationModel> sccModels = new ArrayList<>();
            for (String modelId : scc) {
                sccModels.add(modelMap.get(modelId));
            }
            for (VerificationModel vm : sccModels) {
                sccMap.put(vm, sccModels);
            }
        }

        logger.info("Found SCCs: " + sccs);
    }

    public double verify(String startModelId, String property) throws IOException {
        VerificationModel startModel = modelMap.get(startModelId);
        return verifyModel(startModel, property);
    }

    private double verifyModel(VerificationModel verificationModel, String property) throws IOException {
        logger.info("\n=== Starting verification for model " + verificationModel + " with property " + property + " ===");
        
        List<VerificationModel> currentSCC = sccMap.get(verificationModel);
        logger.info("Current SCC: " + currentSCC);
        
        // Check if we need to use Python solver for this SCC
        if (!usePythonSolver) {
            for (VerificationModel model : currentSCC) {
                logger.info("\nProcessing model: " + model);
                List<DependencyParameter> dependencies = getDependencyParams(model.getModelId());
                logger.info("Dependencies found: " + dependencies);
                
                for (DependencyParameter dep : dependencies) {
                    VerificationModel targetModel = modelMap.get(dep.getModel().getModelId());
                    List<VerificationModel> targetSCC = sccMap.get(targetModel);
                    
                    if (!targetSCC.equals(currentSCC)) {
                        logger.info("  Processing dependency: " + dep);
                        logger.info("  Target model " + targetModel + " is in different SCC: " + targetSCC);
                        double result = verifyModel(targetModel, dep.getDefinition());
                        model.setParameter(dep.getName(), result); 
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
                    for (VerificationModel model : currentSCC) {
                        model.getParameters().clear();
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
        
        double result = performPMC(verificationModel, property);
        logger.info("Final PMC result for " + verificationModel + ": " + result);
        // TODO : export model files with parameters set
        return result;
    }
    
    private void resolveSCCWithPythonSolver(List<VerificationModel> sccModels) {
        logger.info("Starting SCC resolution using Python solver for models: " + sccModels);
        
        try {
            // Prepare input for Python solver
            List<String> inputData = new ArrayList<>();
            VerificationModel startVerificationModel = sccModels.get(0);
            String startModelId = startVerificationModel.getModelId();
            SharedContext sharedContext = SharedContext.getInstance();
            Project project = sharedContext.getProject();
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
            
            for (VerificationModel model : sccModels) {
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
                    VerificationModel targetModel = modelMap.get(dep.getModel().getModelId());
                    
                    if (sccModels.contains(targetModel)) {
                        Model originalTargetModel = getOriginalModel(targetModel.getModelId());
                        if (originalTargetModel == null) {
                            logger.warn("Warning: Could not find original target model for ID: " + targetModel.getModelId());
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
                                           dep.getName();
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
            command.add("--model");
            command.add(modelFilePath);
            command.add("--input");
            command.addAll(inputData);
            //command.add(" -pc");
            
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
            Map<String, Double> results = new HashMap<>();
            
            while ((line = reader.readLine()) != null) {
                logger.info(line);
                
                // Parse results from Python output
                if (line.startsWith("Optimal parameters:")) {
                    // Parse parameters from line like "Optimal parameters: {'param1': 0.123, 'param2': 0.456}"
                    String paramsStr = line.substring(line.indexOf('{') + 1, line.lastIndexOf('}'));
                    String[] params = paramsStr.split(", ");
                    
                    for (String param : params) {
                        String[] keyValue = param.split(":");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim().replace("'", "");
                            double value = Double.parseDouble(keyValue[1].trim());
                            results.put(key, value);
                        }
                    }
                }
            }
            
            int exitCode = process.waitFor();
            logger.info("Python solver exited with code: " + exitCode+" Time elapsed: "+(endTime-startTime));
            
            if (exitCode != 0) {
            	logger.error("Python solver failed with exit code " + exitCode);
                throw new RuntimeException("Python solver failed with exit code " + exitCode);
            }
            
            // Set calculated parameters to models
            if (!results.isEmpty()) {
                for (Map.Entry<String, Double> entry : results.entrySet()) {
                    String paramName = entry.getKey();
                    Double paramValue = entry.getValue();
                    
                    // Find which model this parameter belongs to
                    for (VerificationModel model : sccModels) {
                        for (DependencyParameter dep : getDependencyParams(model.getModelId())) {
                            if (dep.getName().equals(paramName)) {
                                model.setParameter(paramName, paramValue);
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
            //e.printStackTrace();
            throw new RuntimeException("Python solver failed: " + e.getMessage(), e);
        }
    }
    
    private void resolveSCC(List<VerificationModel> sccModels) {
        //mXparser.consolePrintln(false);  // Disable mXparser console output
        logger.info("Starting SCC resolution for models: " + sccModels);
        
        // Store equations and their variables
        List<String> equations = new ArrayList<>();
        Set<String> variableSet = new HashSet<>();
        Map<String, String> equationMap = new HashMap<>();

        // Collect equations and variables
        for (VerificationModel model : sccModels) {
            logger.info("\nGetting dependencies for model: " + model);
            for (DependencyParameter dep : getDependencyParams(model.getModelId())) {
                VerificationModel targetModel = modelMap.get(dep.getModel().getModelId());
                
                if (sccModels.contains(targetModel)) {
                    String rationalFunction = getRationalFunction(targetModel, dep.getDefinition(), null);
                    
                    // Check if rational function is null or empty
                    if (rationalFunction == null || rationalFunction.trim().isEmpty()) {
                        throw new RuntimeException("Failed to get rational function for " + dep.getName() + 
                                                   " in model " + model.getModelId());
                    }
                    
                    String equation = dep.getName() + " = " + rationalFunction;
                    logger.info("  Adding equation: " + equation);
                    
                    // Transform equation to standard form f(x) = 0
                    String transformedEq = transformEquation(equation);
                    equations.add(transformedEq);
                    equationMap.put(dep.getName(), transformedEq);
                    variableSet.add(dep.getName());
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
        Map<String, Double> solutions = new HashMap<>();

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
                solutions.put(variable, newValue);
                logger.info("  " + variable + " = " + newValue);
            }

            converged = iterationConverged;
        }

        if (!converged) {
            logger.warn("\nWarning: Maximum iterations reached without convergence");
        }

        // Set the solved values to the models
        logger.info("\nFinal solutions:");
        for (Map.Entry<String, Double> solution : solutions.entrySet()) {
            logger.info(solution.getKey() + " = " + solution.getValue());
            for (VerificationModel model : sccModels) {
                model.setParameter(solution.getKey(), solution.getValue());
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
    
    private String getRationalFunction(VerificationModel model, String property, List<String> paramNames) {
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

    private double performPMC(VerificationModel model, String property) throws FileNotFoundException {
        logger.info("Performing PMC for " + model + " with property " + property);
        Model originalModel = getOriginalModel(model.getModelId());
        if (originalModel == null) {
        	logger.error("Model not found: " + model.getModelId());
            throw new IllegalArgumentException("Model not found: " + model.getModelId());
        }
        //System.out.println(originalModel.getModelId() + model.getParameters());
		try {
			FileUtils.writeParametersToFile(originalModel.getVerificationFilePath(), originalModel.getHashExternalParameters());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        try {
			FileUtils.writeParametersToFile(originalModel.getVerificationFilePath(), model.getParameters());
		} catch (IOException e) {
			e.printStackTrace();
		}
        // Check if the model is a PRISM-games model
        boolean isPrismGamesModel = false;
        try {
            String modelFilePath = originalModel.getVerificationFilePath();
            if (modelFilePath != null && !modelFilePath.isEmpty()) {
                // Read the first line of the model file
                String firstLine = FileUtils.readFirstLine(modelFilePath);
                // Check if it contains game model identifiers
                if (firstLine != null && 
                    (firstLine.contains("smg") || 
                     firstLine.contains("tsg") || 
                     firstLine.contains("csg") || 
                     firstLine.contains("tptg"))) {
                    isPrismGamesModel = true;
                }
            }
        } catch (IOException e) {
            logger.warn("Could not read model file to check for game model type: " + e.getMessage());
        }

        // If it's a PRISM-games model, use PrismGamesProcessAPI
        if (isPrismGamesModel) {
            logger.info("Detected PRISM-games model type. Using Prism games model checker...");
            try {
                SharedContext sharedContext = SharedContext.getInstance();
                Project project = sharedContext.getProject();
                String prismGamesPath = project.getPrismGamesInstall();
                return PrismGamesProcessAPI.run(originalModel, property, prismGamesPath);
            } catch (IOException prismGamesException) {
                logger.error("Error running PrismGamesProcessAPI: " + prismGamesException.getMessage());
                // Fall back to other methods if PrismGames fails
            }
        }

        // First try with Storm
        try {
        	StormAPI sAPI = new StormAPI();
            return sAPI.run(originalModel, property);
        } catch (Exception stormException) {
            // Extract just the error message without stack trace
            logger.error("Error running Storm: " + stormException.getMessage());
            //throw new Exception();
        }       
        
        // Try with PrismProcessAPI as second fallback
        logger.info("Trying fallback with PRISM...");
        try {
        	SharedContext sharedContext = SharedContext.getInstance();
            Project project = sharedContext.getProject();
            String prismPath = project.getPrismInstall();
            return PrismProcessAPI.run(originalModel, property, prismPath);
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
}