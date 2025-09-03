package data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import parameters.SynthesisObjective;
import project.Project;

public class SynthesisRun {

    private final String runId;
    private final String worldModelName;
    private final String projectConfig;
    private final String timestamp;
    private final HashMap<String, List<SynthesisObjective>> modelSynthesisObjectives;

    private ObservableList<SynthesisSolution> solutions = FXCollections.observableArrayList();

    public SynthesisRun(String runId, Project project) {
        this.runId = runId;
        this.worldModelName = project.getProjectName();
        this.projectConfig = project.generateProjectConfigCacheKey();
        this.modelSynthesisObjectives = new HashMap<>();
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MMdd_HH:mm:ss"));

        for (Model m : project.getModels()) {
            modelSynthesisObjectives.put(m.getModelId(), m.getSynthesisObjectives());
        }

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

    public HashMap<String, List<SynthesisObjective>> getModelSynthesisObjectivesMap() {
        return modelSynthesisObjectives;
    }

    public List<SynthesisObjective> getAllSynthesisObjectives() {
        List<SynthesisObjective> allObjectives = new ArrayList<>();
        for (List<SynthesisObjective> l : modelSynthesisObjectives.values()) {
            allObjectives.addAll(l);
        }
        return allObjectives;
    }

    public ObservableList<SynthesisSolution> getSolutions() {
        return solutions;
    }

    public ObservableList<String> getDetails() {
        ObservableList<String> details = FXCollections.observableArrayList();
        details.add(String.format("Run ID: %s", runId));
        details.add(String.format("World model name: %s", worldModelName));
        details.add(String.format("Project config key: %s", projectConfig));
        details.add(String.format("Timestamp: %s", timestamp));
        details.add(String.format("Synthesis objectives (%d): %s", getAllSynthesisObjectives().size(),
                getModelSynthesisObjectivesMap().entrySet().stream().map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.joining("\n"))));
        details.add(String.format("Number of solutions: %d", solutions.size()));
        return details;
    }

    public String toString() {
        return String.format("Synthesis run %s - %d objectives, %d solutions", runId,
                getAllSynthesisObjectives().size(),
                solutions.size());
    }

}
