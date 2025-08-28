package project.synthesis;

import java.util.HashMap;
import java.util.List;

public class SynthesisSolution {

    private final String runId;
    private final String solutionId;
    private final String modelId;
    private final HashMap<String, String> internalParameterValues; 
    private final HashMap<String, String> objectiveValues;
    private final List<String> objectives;
    private final List<String> constraints;

    public SynthesisSolution(String runId, String solutionId, String model, List<String> objectives,
            List<String> constraints,
            HashMap<String, String> internalParameterValues, HashMap<String, String> objectiveValues) {
        this.runId = runId;
        this.solutionId = solutionId;
        this.modelId = model;
        this.objectives = objectives;
        this.constraints = constraints;
        this.internalParameterValues = internalParameterValues;
        this.objectiveValues = objectiveValues;

    }

    public String getRunId() {
        return runId;
    }

    public String getSolutionId() {
        return solutionId;
    }

    // public void setInternalParameterValues(HashMap<String, String> internalParameterValues) {
    //     this.internalParameterValues = internalParameterValues;
    // }

    public HashMap<String, String> getInternalParameterValues() {
        return internalParameterValues;
    }

    // public void setObjectiveValues(HashMap<String, String> objectiveValues) {
    //     this.objectiveValues = objectiveValues;
    // }

    public HashMap<String, String> getObjectiveValues() {
        return objectiveValues;
    }

    public List<String> getObjectives() {
        return objectives;
    }

    public List<String> getConstraints() {
        return constraints;
    }

    public String getModelId() {
        return modelId;
    }

    public String getDisplayString() {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Synthesis Result %s-%s - Model '%s'",runId, solutionId, modelId));
        // sb.append(String.format("\nConstraints (%d):",
        // constraints.size())).append(String.join("\n", constraints));
        sb.append(String.format("\nObjectives (%d): ", objectives.size()));
        for (String key : objectiveValues.keySet()) {
            sb.append(String.format("\n%s: %s", key, objectiveValues.get(key)));
        }
        sb.append(String.format("\nParameter values (%d): ", internalParameterValues.size()));
        for (String key : internalParameterValues.keySet()) {
            sb.append(String.format("\n%s: %s", key, internalParameterValues.get(key)));
        }
        return sb.toString();

    }

    // TODO: this is a bit inelegant - use different display and non-visible strings
    public String toString() {
        return getDisplayString();
    }

}
