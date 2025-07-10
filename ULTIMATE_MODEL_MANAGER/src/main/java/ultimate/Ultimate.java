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
import org.mariuszgromada.math.mxparser.mXparser;

import javafx.collections.FXCollections;
import parameters.InternalParameter;
import javafx.collections.ObservableList;
import parameters.InternalParameter;

public class Ultimate {

    private final SharedContext sharedContext = SharedContext.getInstance();

    private Project project;
    private String projectFile;

    private Model testingModel;
    private String testingModelID;

    private NPMCVerification verifier;
    private ArrayList<Model> models = new ArrayList<>();

    private String property;
    private HashMap<String, String> internalParameterValues = new HashMap<>();

    java.util.HashMap<String, Double> results = new java.util.HashMap<>();

    public void loadProject(String projectFile) throws IOException {
        this.projectFile = projectFile;
        project = new Project(projectFile);
        sharedContext.setProject(project);
        models.addAll(project.getModels());
        verifier = new NPMCVerification(models);
    }

    public void generateModelInstances() {
        try {
            for (Model m : models) {
                m.setInternalParametersFromHashMap(internalParameterValues);
                FileUtils.writeParametersToFile(m.getVerificationFilePath(), m.getExternalParameters(),
                        m.getInternalParameters());
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public ObservableList<InternalParameter> getInternalParameters() {
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

    public void setInternalParameters(HashMap<String, String> internalParameterValues) {

        // this sets the internal parameter values, but these are only actually
        // "instantiated" into
        // the model when instantiateProject is called (via
        // setInternalParametersFromHashMap)

        this.internalParameterValues = internalParameterValues;
    }

    public void setTargetModelID(String modelID) {
        this.testingModelID = modelID;
        for (Model m : models) {
            if (m.getModelId().equals(modelID)) {
                this.testingModel = m;
                return;
            }
        }

        if (this.testingModel == null) {
            throw new IllegalArgumentException("Model ID not found: " + modelID); // Fail if not found
        }
    }

    // public void CreateEvolvableModelFiles() {
    //     try {
    //         for (Model m : models) {
    //             m.setInternalParametersFromHashMap(internalParameterValues);
    //             FileUtils.writeParametersToFile(m.getVerificationFilePath(), m.getExternalParameters(),
    //                     m.getHashInternalParameters());
    //         }

    //     } catch (IOException e) {
    //         e.printStackTrace();
    //         return;
    //     }
    // }

    public void execute() throws NumberFormatException, IOException {
        if (property == null) {
            for (Property p : testingModel.getProperties()) {
                System.out.println("Verifying property: " + p.getProperty());
                double result_value = verifier.verify(testingModelID, p.getProperty());
                results.put(p.toString(), result_value);
            }
        } else {
            // this checks if the property provided is a file or a property entered as a
            // string (e.g. from the CLI)
            if (new java.io.File(property).isFile()) { // file -> read all lines and verify
                for (String line : java.nio.file.Files.readAllLines(java.nio.file.Paths.get(property))) {
                    System.out.println("Verifying property: " + line.trim());
                    if (!line.trim().isEmpty()) {
                        double result_value = verifier.verify(testingModelID, line.trim());
                        results.put(line.trim(), result_value);
                    }
                }
            } else { // string -> verify single property
                double result_value = verifier.verify(testingModelID, property);
                results.put(property, result_value);
            }
        }
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public java.util.HashMap<String, Double> getResults() {
        return results;
    }

    public String getResultsInfo() {

        StringBuilder resultsInfo = new StringBuilder();
        for (String key : results.keySet()) {
            resultsInfo.append("Property: " + key + "\nResult: " + results.get(key) + "\n\n");
        }
        return resultsInfo.toString();

    }

    public String getProjectFile() {
        return projectFile;
    }

    public String getTestingModelID() {
        return testingModelID;
    }

    public String getProperty() {
        return property;
    }

    public Project getProject() {
        return project;
    }

    public Model getTestingModel() {
        return testingModel;
    }

}