package ultimate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import model.Model;
import project.Project;
import property.Property;
import sharedContext.SharedContext;
import utils.FileUtils;
import verification.NPMCVerification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.collections.FXCollections;
import parameters.InternalParameter;
import javafx.collections.ObservableList;
import parameters.InternalParameter;
import jmetal.core.SolutionSet;
import jmetal.core.Solution;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import evochecker.EvoChecker;
import evochecker.lifecycle.IUltimate;
import parameters.IStaticParameter;

public class Ultimate {

    // private Project project;
    // private String projectFilePath;

    private Model targetModel;

    private NPMCVerification verifier;
    private ArrayList<Model> models = new ArrayList<>();

    private String property;
    private String objectivesConstraints;

    private HashMap<String, String> internalParameterValuesHashMap = new HashMap<>();
    private HashMap<String, String> externalParameterValuesHashMap = new HashMap<>();

    private EvoChecker evoChecker;
    private SolutionSet synthesisedParameters;
    private Path evolvableProjectFilePath;

    private String mode;

    private boolean verbose;

    java.util.HashMap<String, String> results = new java.util.HashMap<>();

    public Ultimate() {

    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    // public void setProject(Project project) {
    // this.project = project;
    // this.projectFilePath = project.getPath();
    // }

    // public void loadProjectFromFile(String filepath) throws IOException {
    // this.projectFilePath = filepath;
    // project = new Project(filepath);
    // // issue is that the project import requires the project path which is only
    // initialised AFTER the project is loaded.
    // this.setProject(project);
    // }

    public void initialiseModels() {
        Set<Model> projectModels = SharedContext.getProject().getModels();
        if (projectModels == null || projectModels.size() == 0) {
            System.err
                    .println("Warning: ULTIMATE tried to initialise models, but no models were found in the project.");
        }
        models.addAll(SharedContext.getProject().getModels());
        verifier = new NPMCVerification(models);
    }

    public void generateModelInstances() {
        try {
            for (Model m : models) {
                m.setInternalParametersFromHashMap(internalParameterValuesHashMap);
                m.setExternalParametersFromHashMap(externalParameterValuesHashMap);
                FileUtils.writeParametersToFile(m.getVerificationFilePath(), m.getExternalParameters(),
                        m.getInternalParameters());
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public ObservableList<InternalParameter> getInternalParameters() {
        Project project = SharedContext.getProject();
        if (project == null) {
            throw new IllegalStateException("Project has not been instantiated yet.");
        }

        ArrayList<Model> models = new ArrayList<>(project.getModels());
        ObservableList<InternalParameter> internalParameters = FXCollections.observableArrayList();
        if (models.isEmpty()) {
            throw new IllegalStateException("No models found in the project.");
        }

        for (Model model : models) {
            internalParameters.addAll(model.getInternalParameters());
        }

        return internalParameters;
    }

    public void setInternalParameters(HashMap<String, String> internalParameterValuesHashMap) {

        // this sets the internal parameter values, but these are only actually
        // "instantiated" into
        // the model when generateModelInstances is called (via
        // setInternalParametersFromHashMap)

        this.internalParameterValuesHashMap = internalParameterValuesHashMap;
    }

    public void setExternalParameters(HashMap<String, ?> params) {
        HashMap<String, String> hm = new HashMap<>();
        for (Map.Entry<String, ?> e : params.entrySet()) {
            hm.put(e.getKey(), e.getValue().toString());
        }
        this.externalParameterValuesHashMap = hm;
    }

    public void setTargetModelById(String modelID) {
        System.out.println("setTestingModel: " + models.stream().map(Model::getModelId).collect(Collectors.toList()));
        Model matchingModel = models.stream().filter(model -> model.getModelId().equals(modelID)).findFirst().orElse(null);
        this.targetModel = matchingModel;

        if (this.targetModel == null) {
            throw new IllegalArgumentException("Model ID not found: " + modelID); // Fail if not found
        }
    }

    public void generateEvolvableModelFiles() {

        Project project = SharedContext.getProject();

        try {

            Path tempDir = Files.createTempDirectory("ultimate_evoproject_");
            Path sourceProjectPath = Paths.get(project.getPath());
            Path targetProjectPath = tempDir.resolve(sourceProjectPath.getFileName());
            evolvableProjectFilePath = targetProjectPath;
            Files.copy(sourceProjectPath, targetProjectPath, StandardCopyOption.REPLACE_EXISTING);

            for (Model m : models) {

                Path sourceModelPath = sourceProjectPath.resolve(m.getFilePath());
                Path targetModelPath = tempDir.resolve(sourceModelPath.getFileName());
                Files.copy(sourceModelPath, targetModelPath, StandardCopyOption.REPLACE_EXISTING);

                FileUtils.writeEvolvablesToFile(targetModelPath.toString(), m.getInternalParameters());
                FileUtils.writeParametersToFile(targetModelPath.toString(), m.getExternalParameters(),
                        null);
                FileUtils.writeDummyDependenciesToFile(targetModelPath.toString());
            }

        } catch (Exception e) {
            System.err.println("Error generating evolvable model files: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

    }

    public void execute() throws NumberFormatException, IOException {
        if (verbose)
            System.out.print("Executing ULTIMATE for property " + property);
        resetResults();
        if (property == null) {
            for (Property p : targetModel.getProperties()) {
                if (verbose)
                    System.out.println("Verifying property: " + p.getProperty());
                String result_value = verifier.verify(targetModel.getModelId(), p.getProperty());
                System.out.println("Result: " + result_value);
                results.put(p.toString(), result_value);
            }
        } else {
            // this checks if the property provided is a file or a property entered as a
            // string (e.g. from the CLI)
            if (new java.io.File(property).isFile()) { // file -> read all lines and verify
                for (String line : java.nio.file.Files.readAllLines(java.nio.file.Paths.get(property))) {
                    if (verbose)
                        System.out.println("Verifying property: " + line.trim());
                    if (!line.trim().isEmpty()) {
                        String result_value = verifier.verify(targetModel.getModelId(), line.trim());
                        results.put(line.trim(), result_value);
                    }
                }
            } else { // string -> verify single property
                String result_value = verifier.verify(targetModel.getModelId(), property);
                if (verbose)
                    System.out.println(property + ": " + result_value);
                results.put(property, result_value);
            }
        }
    }

    // public void synthesiseInternalParameters() {
    // /*
    // * - Instantiate EC
    // * - Initialise EC
    // * - Write evolvable world model (EWM) files
    // * - Give EC the OCs, the EWMs, and the ULTIMATE instance itself
    // * - Set results
    // */

    // }

    public void instantiateEvoCheckerInstance(IUltimate ultimateInstance) {
        evoChecker = new EvoChecker();
        evoChecker.setUltimateInstance(ultimateInstance);
    }

    public void initialiseEvoCheckerInstance(String projectFile) {
        if (evoChecker == null) {
            System.err.println(
                    "Attempted to initialise EvoChecker, but it has not yet been instantiated. Call 'instantiateEvoCheckerInstance' first");
            System.exit(1);
        }
        evoChecker.setConfigurationFile(System.getenv("ULTIMATE_DIR") + "/evochecker_config.properties", projectFile,
                null);
    }

    public void executeEvoChecker() {
        evoChecker.start();
        evoChecker.printStatistics();
    }

    public void setObjectivesConstraints(String propertyFileOrString) {
        this.objectivesConstraints = propertyFileOrString;
    }

    public void setVerificationProperty(String propertyFileOrString) {
        this.property = propertyFileOrString;
    }

    public void setVerificationProperty(Property property) {
        this.property = property.toString();
    }

    // TODO: safely rename to getVerificationResults
    public java.util.HashMap<String, String> getResults() {
        return results;
    }

    // public double[][] getSynthesisResults() {
    // SolutionSet results = evoChecker.getSolutions();
    // double[][] resultsMatrix = results.writeObjectivesToMatrix();

    // return resultsMatrix;
    // }

    public void writeSynthesisResultsToFile() {

        try {
            evoChecker.ExportToFile();
        } catch (Exception e) {
            System.err.println("Couldn't write the synthesis results to file.");
            e.printStackTrace();
        }

        // double[][] resultsMatrix = getSynthesisResults();
        // System.out.println("\nResults(" + resultsMatrix.length + "," +
        // resultsMatrix[0].length + "):\n");

        // try {
        // java.io.FileWriter writer = new java.io.FileWriter("synthesis_results.txt");
        // for (double[] row : resultsMatrix) {
        // for (int i = 0; i < row.length; i++) {
        // System.out.println(row[i]);
        // writer.write(Double.toString(row[i]));
        // if (i < row.length - 1) {
        // writer.write(", ");
        // }
        // }
        // writer.write(System.lineSeparator());
        // }
        // writer.close();
        // } catch (IOException e) {
        // System.err.println("Error writing synthesis results to file: " +
        // e.getMessage());
        // }

    }

    public void resetResults() {
        results = new java.util.HashMap<>();
    }

    public String generateModelConfigurationIdentifier() throws IOException {

        StringBuilder configIdBuilder = new StringBuilder();

        // save all eps, ips, to string
        for (Model m : this.models) {
            for (IStaticParameter p : m.getStaticParameters()) {
                configIdBuilder.append(p.getName() + " : " + p.getValue() + "\n");
            }
        }

        return configIdBuilder.toString();
    }

    public HashMap<String, String> generateModelConfigurationHashMap() throws IOException {

        HashMap<String, String> modelConfigurationHashMap = new HashMap<>();

        // save all eps, ips, to string
        for (Model m : this.models) {
            for (IStaticParameter p : m.getStaticParameters()) {
                modelConfigurationHashMap.put(p.getName(), p.getValue());
            }
        }

        return modelConfigurationHashMap;

    }

    public String getVerificationResultsInfo() {

        StringBuilder resultsInfo = new StringBuilder();
        for (String key : results.keySet()) {
            resultsInfo.append("Property: " + key + "\nResult: " + results.get(key) + "\n\n");
        }
        return resultsInfo.toString();

    }

    // public String getProjectFilePath() {
    // return projectFilePath;
    // }

    // public String getProjectDirectoryPath(){
    // if (projectFilePath == null) {
    // return null;
    // }
    // Path path = Paths.get(projectFilePath);
    // Path parent = path.getParent();
    // return parent != null ? parent.toString() : null;
    // }

    public String getTargetModelId() {
        return targetModel.getModelId();
    }

    public String getProperty() {
        return property;
    }

    public String getObjectivesConstraints() {
        return objectivesConstraints;
    }

    // public Project getProject() {
    // return project;
    // }

    public Model getTargetModel() {
        return targetModel;
    }

    public Path getEvolvableProjectFilePath() {
        return evolvableProjectFilePath;
    }

}