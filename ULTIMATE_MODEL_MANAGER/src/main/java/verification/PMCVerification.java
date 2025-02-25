package verification;

import verification.DependencyGraph;
import java.util.*;
import model.Model;
import parameters.DependencyParameter;
import utils.FileUtils;

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
        return verifyModel(startModel, property);
    }

    private double verifyModel(VerificationModel verificationModel, String property) {
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
    
    private void resolveSCC(List<VerificationModel> sccModels) {
        System.out.println("Starting SCC resolution for models: " + sccModels);

        int numVariables = sccModels.size();
        double[][] coefficients = new double[numVariables][numVariables]; // Matrix A
        double[] constants = new double[numVariables]; // Vector B

        Map<String, Integer> paramIndexMap = new HashMap<>();
        List<String> paramNames = new ArrayList<>();
        int index = 0;

        for (VerificationModel model : sccModels) {
            for (DependencyParameter dep : getDependencyParams(model.getModelId())) {
                VerificationModel targetModel = modelMap.get(dep.getModel().getModelId());

                if (sccModels.contains(targetModel)) {
                    if (!paramIndexMap.containsKey(dep.getName())) {
                        paramIndexMap.put(dep.getName(), index);
                        paramNames.add(dep.getName());
                        index++;
                    }

                    int paramIndex = paramIndexMap.get(dep.getName());
                    RationalFunction rationalFunction = getRationalFunction(targetModel, dep.getDefinition(), paramNames);

                    coefficients[paramIndex] = rationalFunction.getCoefficients();
                    constants[paramIndex] = rationalFunction.getConstant();

                    System.out.println("  Added equation: " + dep.getName());
                }
            }
        }

        if (paramIndexMap.size() < numVariables) {
            throw new IllegalStateException("Equation system is incomplete! Missing parameters.");
        }

        Equation equationSystem = new Equation(coefficients, constants);
        double[] solution = equationSystem.solve();

        for (int i = 0; i < solution.length; i++) {
            String paramName = paramNames.get(i);
            for (VerificationModel model : sccModels) {
                model.setParameter(paramName, solution[i]);
            }
        }

        System.out.println("SCC solution complete: " + paramNames + " → " + Arrays.toString(solution));
    }

    
    private List<DependencyParameter> getDependencyParams(String modelId) {
        for (Model model : originalModels) {
            if (model.getModelId().equals(modelId)) {
                return model.getDependencyParameters();
            }
        }
        return new ArrayList<>();
    }
    
    private RationalFunction getRationalFunction(VerificationModel model, String property, List<String> paramNames) {
        System.out.println("Performing parametric MC for " + model + " with property " + property);
        
        Model originalModel = getOriginalModel(model.getModelId());
        if (originalModel == null) {
            throw new IllegalArgumentException("Model not found: " + model.getModelId());
        }

        String equationStr= StormAPI.runPars(originalModel, property, "/Users/micahbassett/Desktop/storm/build/bin/storm-pars");
        System.out.println("Received equation: " + equationStr);

        return new RationalFunction(equationStr, paramNames);
    }

    
    

    private double performPMC(VerificationModel model, String property) {
        System.out.println("Performing PMC for " + model + " with property " + property);
        Model originalModel = getOriginalModel(model.getModelId());
        if (originalModel == null) {
            throw new IllegalArgumentException("Model not found: " + model.getModelId());
        }
        FileUtils.updateModelFileResults(originalModel, model.getParameters());

        return StormAPI.run(originalModel, property, "/Users/micahbassett/Desktop/storm/build/bin/storm");
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

 class RationalFunction {
    private final double[] coefficients;
    private final double constant;

    public RationalFunction(String equationStr, List<String> variableNames) {
        this.coefficients = new double[variableNames.size()];
        this.constant = parseEquation(equationStr, variableNames);
    }

    private double parseEquation(String equationStr, List<String> variableNames) {
        String[] terms = equationStr.split("\\+"); // Splitting terms

        double constantTerm = 0.0;
        for (String term : terms) {
            term = term.trim();
            if (term.matches("-?\\d+(\\.\\d+)?")) {  // Constant term
                constantTerm += Double.parseDouble(term);
            } else {
                for (int i = 0; i < variableNames.size(); i++) {
                    if (term.contains(variableNames.get(i))) {
                        String coefficientStr = term.replace(variableNames.get(i), "").trim();
                        this.coefficients[i] = coefficientStr.isEmpty() ? 1.0 : Double.parseDouble(coefficientStr);
                    }
                }
            }
        }
        return constantTerm;
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    public double getConstant() {
        return constant;
    }
}