package ultimate;

import java.io.IOException;
import java.util.ArrayList;

import model.Model;
import project.Project;
import property.Property;
import sharedContext.SharedContext;
import utils.FileUtils;
import verification.NPMCVerification;
import org.mariuszgromada.math.mxparser.mXparser;

public class Ultimate {

    private final SharedContext sharedContext = SharedContext.getInstance();

    private Project project;
    private String projectFile;

    private Model testingModel;
    private String testingModelID;

    private NPMCVerification verifier;
    private ArrayList<Model> models = new ArrayList<>();

    private String property;

    java.util.HashMap<String, Double> results = new java.util.HashMap<>();

    public void loadProjectFromFile(String projectFile) {
        this.projectFile = projectFile;
        try {
            project = new Project(projectFile);
            sharedContext.setProject(project);
            models.addAll(project.getModels());
            verifier = new NPMCVerification(models);
            // update the mode files here
            for (Model m : models) {
                FileUtils.writeParametersToFile(m.getVerificationFilePath(), m.getHashExternalParameters());
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void setModelID(String modelID) {
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

    public void setProperty(String property) {
        this.property = property;
    }

    public void execute() throws NumberFormatException, IOException {
        if (property == null) {
            for (Property p : testingModel.getProperties()) {
                double result_value = verifier.verify(testingModelID, p.getProperty());
                results.put(p.toString(), result_value);
            }
        } else {
            // this might result in a directory entry in the results dict.
            // Not a big deal but a bit messy. Although the user shouldn't use it like that
            // anyway

            if (new java.io.File(property).isFile()) {
                for (String line : java.nio.file.Files.readAllLines(java.nio.file.Paths.get(property))) {
                    if (!line.trim().isEmpty()) {
                        double result_value = verifier.verify(testingModelID, line.trim());
                        results.put(line.trim(), result_value);
                    }
                }
            } else {
                double result_value = verifier.verify(testingModelID, property);
                results.put(property, result_value);
            }
        }
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