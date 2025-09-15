package data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import parameters.InternalParameter;
import parameters.SynthesisGoal;
import project.Project;
import sharedContext.SharedContext;

public class SynthesisRun {

    private final String runId;
    private final String worldModelName;
    private final String projectConfig;
    private final String timestamp;
    private String paretoFrontFilePath;
    private String paretoSetFilePath;
    private final HashMap<String, List<String>> modelSynthesisObjectivesDefinitionsMap;
    private final HashMap<String, List<String>> modelInternalParameterNamesMap;

    private ObservableList<SynthesisSolution> solutions = FXCollections.observableArrayList();

    public SynthesisRun(String runId, Project project) {
        this.runId = runId;
        this.worldModelName = project.getProjectName();
        this.projectConfig = project.generateParameterConfigurationKey();
        this.modelInternalParameterNamesMap = new HashMap<>();
        this.modelSynthesisObjectivesDefinitionsMap = new HashMap<>();
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MMdd_HH:mm:ss"));

        for (Model m : project.getModels()) {
            List<String> goalDefinitions = new ArrayList<>();
            List<String> internalParameterNames = new ArrayList<>();
            for (SynthesisGoal g : m.getSynthesisGoals()) {
                goalDefinitions.add(g.getDefinition());
            }
            for (InternalParameter ip : m.getInternalParameters()) {
                internalParameterNames.add(ip.getNameInModel());
            }
            modelSynthesisObjectivesDefinitionsMap.put(m.getModelId(), goalDefinitions);
            modelInternalParameterNamesMap.put(m.getModelId(), internalParameterNames);

        }

    }

    // TODO: this is mostly used to get .size. Just cache it in init?
    public List<String> getAllInternalParameterNames() {
        List<String> allInternalParameters = new ArrayList<>();
        for (List<String> l : modelInternalParameterNamesMap.values()) {
            allInternalParameters.addAll(l);
        }
        return allInternalParameters;
    }

    public List<String> getAllSynthesisGoalDefinitions() {
        List<String> allGoals = new ArrayList<>();
        for (List<String> l : modelSynthesisObjectivesDefinitionsMap.values()) {
            allGoals.addAll(l);
        }
        return allGoals;
    }

    public List<String> getAllSynthesisObjectiveDefinitions() {
        List<String> allObjectives = new ArrayList<>();
        for (List<String> l : modelSynthesisObjectivesDefinitionsMap.values()) {
            for (String g : l) {
                if (g.toLowerCase().startsWith("objective")) {
                    allObjectives.add(g);
                }
            }
        }
        return allObjectives;
    }

    public List<String> getAllSynthesisConstraintDefinitions() {
        List<String> allConstraints = new ArrayList<>();
        for (List<String> l : modelSynthesisObjectivesDefinitionsMap.values()) {
            for (String g : l) {
                if (g.toLowerCase().startsWith("constraint")) {
                    allConstraints.add(g);
                }
            }
        }
        return allConstraints;
    }

    public ObservableList<SynthesisSolution> getSolutions() {
        return solutions;
    }

    // TODO: clean up
    public ObservableList<String> getDetails() {
        ObservableList<String> details = FXCollections.observableArrayList();
        details.add(String.format("Run ID: %s", runId));
        details.add(String.format("World model name: %s", worldModelName));
        details.add(String.format("Timestamp: %s", timestamp));
        details.add(String.format("Synthesis goals (%d): %s", getAllSynthesisGoalDefinitions().size(),
                getModelSynthesisObjectivesDefinitionsMap().entrySet().stream()
                        .map(e -> e.getKey() + ": " + String.join("\t\n", e.getValue()))
                        .collect(Collectors.joining("\n"))));
        details.add(String.format("Number of solutions: %d", solutions.size()));
        details.add(String.format("Internal parameters: %s", modelInternalParameterNamesMap.entrySet().stream()
                .map(e -> e.getKey() + ": " + String.join("\t\n", e.getValue())).collect(Collectors.joining("\n"))));
        return details;
    }

    public String toString() {
        return String.format("Synthesis run %s - %d goals, %d solutions", runId,
                getAllSynthesisGoalDefinitions().size(),
                solutions.size());
    }

    public HashMap<String, List<String>> getModelInternalParameterNamesMap() {
        return modelInternalParameterNamesMap;
    }

    public void setParetoFrontFilePath(String path) {
        this.paretoFrontFilePath = path;
    }

    public String getParetoFrontFilePath() {
        return this.paretoFrontFilePath;
    }

    public void setParetoSetFilePath(String path) {
        this.paretoSetFilePath = path;
    }

    public String getParetoSetFilePath() {
        return this.paretoSetFilePath;
    }

    public void addSolution(SynthesisSolution solution) {
        this.solutions.add(solution);
    }

    public void addSolutions(List<SynthesisSolution> solutions) {
        this.solutions.addAll(solutions);
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

    public HashMap<String, List<String>> getModelSynthesisObjectivesDefinitionsMap() {
        return modelSynthesisObjectivesDefinitionsMap;
    }

}
