package verification;


import verification.DependencyGraph;
import verification.Equation;
//import verification_engine.prism.PrismAPI;
import verification.StormAPI;
import prism.PrismException;
import utils.FileUtils;

//import com.irurueta.mathsolver.MathSolver;
//import com.irurueta.mathsolver.NonLinearEquation;
import org.mariuszgromada.math.mxparser.*;

import model.Model;
import parameters.DependencyParameter;

import java.io.FileNotFoundException;
import java.util.*;

public class PMCVerification {
    static class VerificationModel {
        private String modelId;
        private HashMap<String, Double> parameters;
        
        public VerificationModel(String modelId) {
            this.modelId = modelId;
            this.parameters = new HashMap<>();
        }
        
        public void setParameter(String paramName, double value) {
            this.parameters.put(paramName, value);
            System.out.println("    → Setting parameter " + paramName + " = " + value + " for model " + modelId);
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

    public PMCVerification(ArrayList<Model> models) {
    	//License.iConfirmNonCommercialUse("Sinem, University of York");  // Add this line to confirm license
    	mXparser.consolePrintln(false);  // Disable mXparser console output of math lib
        this.originalModels = models;
        this.modelMap = new HashMap<>();
        initializeFromModels(models);
    }

    private void initializeFromModels(ArrayList<Model> models) {
        System.out.println("Initializing verification from models...");
        
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

        System.out.println("Found SCCs: " + sccs);
    }

    public double verify(String startModelId, String property) {
        VerificationModel startModel = modelMap.get(startModelId);
        try {
			return verifyModel(startModel, property);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return 0.0;
    }

    private double verifyModel(VerificationModel verificationModel, String property) throws FileNotFoundException, PrismException {
        System.out.println("\n=== Starting verification for model " + verificationModel + " with property " + property + " ===");
        
        List<VerificationModel> currentSCC = sccMap.get(verificationModel);
        System.out.println("Current SCC: " + currentSCC);
        
        for (VerificationModel model : currentSCC) {
            System.out.println("\nProcessing model: " + model);
            List<DependencyParameter> dependencies = getDependencyParams(model.getModelId());
            System.out.println("Dependencies found: " + dependencies);
            
            for (DependencyParameter dep : dependencies) {
                VerificationModel targetModel = modelMap.get(dep.getModel().getModelId());
                List<VerificationModel> targetSCC = sccMap.get(targetModel);
                
                if (!targetSCC.equals(currentSCC)) {
                    System.out.println("  Processing dependency: " + dep);
                    System.out.println("  Target model " + targetModel + " is in different SCC: " + targetSCC);
                    double result = verifyModel(targetModel, dep.getDefinition());
                    model.setParameter(dep.getName(), result); 
                    
                } else {
                    System.out.println("  Skipping dependency: " + dep + " (same SCC)");
                }
            }
        }
        
        if (currentSCC.size() > 1) {
            System.out.println("\nResolving SCC for models: " + currentSCC);
            resolveSCC(currentSCC);
        }
        
        double result = performPMC(verificationModel, property);
        System.out.println("Final PMC result for " + verificationModel + ": " + result);
        return result;
    }
    
//    private void resolveSCC(List<VerificationModel> sccModels) {
//        Map<String, String> equations = new HashMap<>();
//        List<Map.Entry<VerificationModel, String>> paramList = new ArrayList<>();
//        
//        System.out.println("Starting SCC resolution for models: " + sccModels);
//        
//        for (VerificationModel model : sccModels) {
//            System.out.println("\nGetting dependencies for model: " + model);
//            for (DependencyParameter dep : getDependencyParams(model.getModelId())) {
//                VerificationModel targetModel = modelMap.get(dep.getModelID());
//                if (sccModels.contains(targetModel)) {
//                    String equation = getRationalFunction(targetModel, dep.getDefinition());
//                    System.out.println("  Adding equation: " + dep.getName() + " = " + equation);
//                    equations.put(dep.getName(), equation);
//                    paramList.add(new AbstractMap.SimpleEntry<>(model, dep.getName()));
//                }
//            }
//        }
//        
//        System.out.println("\nSolving equations: " + equations);
//        Map<String, Double> solution = solveEquations(equations);
//        System.out.println("Solutions found: " + solution);
//        
//        for (Map.Entry<VerificationModel, String> param : paramList) {
//            param.getKey().setParameter(param.getValue(), solution.get(param.getValue()));
//        }
//    }
    
    private void resolveSCC(List<VerificationModel> sccModels) {
    	 mXparser.consolePrintln(false);  // Disable mXparser console output
        System.out.println("Starting SCC resolution for models: " + sccModels);
        
        // Store equations and their variables
        List<String> equations = new ArrayList<>();
        Set<String> variableSet = new HashSet<>();
        Map<String, String> equationMap = new HashMap<>();

        // Collect equations and variables
        for (VerificationModel model : sccModels) {
            System.out.println("\nGetting dependencies for model: " + model);
            for (DependencyParameter dep : getDependencyParams(model.getModelId())) {
                VerificationModel targetModel = modelMap.get(dep.getModel().getModelId());
                
                if (sccModels.contains(targetModel)) {
                    String rationalFunction = getRationalFunction(targetModel, dep.getDefinition(), null);
                    String equation = dep.getName() + " = " + rationalFunction;
                    System.out.println("  Adding equation: " + equation);
                    
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
        System.out.println("\nSolving equation system:");
        System.out.println("Variables: " + Arrays.toString(variables));
        System.out.println("Equations: " + equations);

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
            System.out.println("\nIteration " + (iteration + 1) + ":");
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
                    System.out.println("  Failed to solve for " + variable);
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
                System.out.println("  " + variable + " = " + newValue);
            }

            converged = iterationConverged;
        }

        if (!converged) {
            System.out.println("\nWarning: Maximum iterations reached without convergence");
        }

        // Set the solved values to the models
        System.out.println("\nFinal solutions:");
        for (Map.Entry<String, Double> solution : solutions.entrySet()) {
            System.out.println(solution.getKey() + " = " + solution.getValue());
            for (VerificationModel model : sccModels) {
                model.setParameter(solution.getKey(), solution.getValue());
            }
        }
    }
    
    private String transformEquation(String equation) {
        // Split equation into left and right sides
        String[] sides = equation.split("\\s*=\\s*");
        if (sides.length != 2) {
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
        System.out.println("Performing parametric MC for " + model + " with property " + property);
        
        Model originalModel = getOriginalModel(model.getModelId());
        if (originalModel == null) {
            throw new IllegalArgumentException("Model not found: " + model.getModelId());
        }
        StormAPI sAPI = new StormAPI();
        String equationStr = sAPI.runPars(originalModel, property);
        System.out.println("Received equation: " + equationStr);

        return equationStr;
    }
    
    

    private double performPMC(VerificationModel model, String property) throws FileNotFoundException, PrismException {
        System.out.println("Performing PMC for " + model + " with property " + property);
        Model originalModel = getOriginalModel(model.getModelId());
        if (originalModel == null) {
            throw new IllegalArgumentException("Model not found: " + model.getModelId());
        }
        FileUtils.updateModelFileResults(originalModel, model.getParameters());
        StormAPI spAPI = new StormAPI();
        return spAPI.run(originalModel, property);
        //return PrismAPI.run(originalModel, property, true);
         
        
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
