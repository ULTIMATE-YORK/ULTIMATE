package data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import parameters.ExternalParameter;
import parameters.RangedExternalParameter;
import project.Project;
import sharedContext.SharedContext;
import utils.ParameterUtilities;

public class VerificationRun {

    private final String runId;
    private final String worldModelName;
    private final String projectConfig;
    private final String timestamp;
    private final String propertyDefinition;
    private final String modelId;
    private boolean retrievedFromCache;
    // private final List<HashMap<String, String>> results;
    private final List<VerificationResult> results;
    private final HashMap<String, List<String>> modelRangedExternalParameterNamesMap;
    private final List<String> allRangedExternalParameterNames;
    private final HashMap<String, HashMap<String, String>> rangedExternalParameterValuesPerModel;
    private String dataFilePath;

    // NOTE: results are a hashmap<String, String> because multi-property
    // verifications are supported by ULTIMATE,
    // but not by the GUI. For now this datatype is mostly redundant, but should be
    // useful for extension.

    public VerificationRun(String runId, String modelId, String propertyDefinition,
            Project project, boolean retrievedFromCache) {
        this.runId = runId;
        this.modelId = modelId;
        this.propertyDefinition = propertyDefinition;
        this.worldModelName = project.getProjectName();
        this.projectConfig = project.generateParameterConfigurationKey();
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MMdd_HH:mm:ss"));
        this.retrievedFromCache = retrievedFromCache;
        this.results = new ArrayList<>();
        this.modelRangedExternalParameterNamesMap = new HashMap<>();
        this.rangedExternalParameterValuesPerModel = new HashMap<>(project.getRangedParameterValuesPerModel());

        for (Model m : project.getModels()) {
            List<String> externalParameterNames = new ArrayList<>();

            for (ExternalParameter ep : m.getExternalParameters()) {
                if (ep instanceof RangedExternalParameter) {
                    externalParameterNames.add(ep.getNameInModel());
                }
            }
            modelRangedExternalParameterNamesMap.put(m.getModelId(), externalParameterNames);
        }

        List<String> allExternalParameterNames = new ArrayList<>();
        for (List<String> l : modelRangedExternalParameterNamesMap.values()) {
            allExternalParameterNames.addAll(l);
        }
        this.allRangedExternalParameterNames = allExternalParameterNames;

    }

    public List<String> getAllRangedExternalParameterNames() {
        return allRangedExternalParameterNames;
    }

    public List<String> getModelNames() {
        return new ArrayList<>(modelRangedExternalParameterNamesMap.keySet());
    }

    public List<String> getUniqueParameterNames() {

        List<String> modelNames = getModelNames();
        List<String> uniqueParameterNames = new ArrayList<>();
        for (int i = 0; i < modelNames.size(); i++) {
            String modelName = modelNames.get(i);
            if (rangedExternalParameterValuesPerModel.get(modelName).size() == 0) {
                continue;
            } else {
                List<String> parameterNames = new ArrayList<>(
                        rangedExternalParameterValuesPerModel.get(modelName).keySet());
                for (String pName : parameterNames) {
                    uniqueParameterNames.add(ParameterUtilities.generateUniqueParameterId(modelName, pName));
                }
            }
        }

        return uniqueParameterNames;

    }

    public String toString() {
        if (results.size() == 1) {
            return String.format("Verification: Model '%s'\nProperty '%s':\nValue: %s", modelId, propertyDefinition,
                    results.get(0).getVerifiedPropertyValueMap().get(propertyDefinition));
        } else if (results.size() > 1) {
            return String.format("Ranged Verification: Model '%s'\nProperty '%s':\n%d results", modelId,
                    propertyDefinition,
                    results.size());
        } else {
            return String.format("Verification: Model '%s', Property '%s: no results.", modelId, propertyDefinition);
        }
    }

    public ObservableList<String> getDetails() {
        ObservableList<String> details = FXCollections.observableArrayList();
        details.add(String.format("Run ID: %s", runId));
        details.add(String.format("World model name: %s", worldModelName));
        // details.add("World model parameters: "
        // +
        // (projectConfig.equals(SharedContext.getProject().generateParameterConfigurationKey())
        // ? "Identical to current configuration"
        // : "!OLD: parameters differ from present configuration!"));
        details.add(String.format("Timestamp: %s", timestamp));
        details.add(String.format("Ranged external parameters (%d):\n\n%s", getAllRangedExternalParameterNames().size(),
                modelRangedExternalParameterNamesMap.entrySet().stream()
                        .map(e -> e.getValue() != null ? e.getKey() + ": " + String.join(", ", e.getValue()) : "")
                        .collect(Collectors.joining("\n"))));
        details.add(String.format("Number of results: %d", results.size()));
        return details;
    }

    public ObservableList<String> getResultStrings() {
        ObservableList<String> strings = FXCollections.observableArrayList();
        for (int i = 0; i < results.size(); i++) {
            strings.add(results.get(i).getDisplayString());
        }
        return strings;
    }

    public String getRunId() {
        return runId;
    }

    public String getWorldModelName() {
        return worldModelName;
    }

    public String getProjectConfig() {
        return projectConfig;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public List<VerificationResult> getResults() {
        return results;
    }

    public void setResults(List<VerificationResult> results) {
        this.results.addAll(results);
    }

    public void addResult(VerificationResult result) {
        this.results.add(result);
    }

    public boolean getRetrievedFromCache() {
        return retrievedFromCache;
    }

    public String getPropertyDefinition() {
        return propertyDefinition;
    }

    public String getModelId() {
        return modelId;
    }

    public void setRetrievedFromCache(boolean retrievedFromCache) {
        this.retrievedFromCache = retrievedFromCache;
    }

    public void ExportToFile(String filePath, boolean makeTemporary) throws IOException {

        File f = null;
        if (makeTemporary) {
            f = Files.createTempFile(filePath, "_temp").toFile();
            f.deleteOnExit();
        } else {
            Path p = Path.of(filePath);
            if (Files.exists(p)) {
                Files.delete(p);
            }
            f = Files.createFile(p).toFile();
        }

        List<String> modelNames = getModelNames();
        List<String> uniqueParameterNames = getUniqueParameterNames();

        String header = String.join("\t", uniqueParameterNames) + "\t"
                + String.join("\t", getPropertyDefinition());

        BufferedWriter br = new BufferedWriter(new FileWriter(f));
        br.write(header);
        br.newLine();
        for (VerificationResult vr : results) {

            List<String> parameterValues = new ArrayList<>();
            for (String mn : modelNames) {
                List<String> parametersInModel = new ArrayList<>(
                        rangedExternalParameterValuesPerModel.get(mn).keySet());
                for (String pn : parametersInModel) {
                    if (vr.getRangedParameterValues().get(mn).size() > 0) {
                        parameterValues.add(vr.getRangedParameterValues().get(mn).get(pn));
                    }
                }
            }

            String line = String.join("\t", parameterValues) + "\t"
                    + vr.getVerifiedPropertyValueMap().get(getPropertyDefinition());
            br.write(line);
            br.newLine();
        }

        br.close();
        dataFilePath = f.getAbsolutePath();

    }

    public String getDataFile() {
        return dataFilePath;
    }

}
