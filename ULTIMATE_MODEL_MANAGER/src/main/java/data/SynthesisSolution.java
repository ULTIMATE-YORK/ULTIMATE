package data;

import java.util.HashMap;

public class SynthesisSolution {

    private final String solutionId;
    private final HashMap<String, String> internalParameterValues; 
    private final HashMap<String, String> objectiveValues;

    public SynthesisSolution(String solutionId,
            HashMap<String, String> internalParameterValues, HashMap<String, String> objectiveValues) {
        this.solutionId = solutionId;
        this.internalParameterValues = internalParameterValues;
        this.objectiveValues = objectiveValues;

    }

    public String getSolutionId() {
        return solutionId;
    }


    public HashMap<String, String> getInternalParameterValues() {
        return internalParameterValues;
    }

    public HashMap<String, String> getObjectiveValues() {
        return objectiveValues;
    }

    public String getDisplayString() {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n>>>Objective values (%d): ", objectiveValues.size()));
        for (String key : objectiveValues.keySet()) {
            sb.append(String.format("\n%s: %s", key, objectiveValues.get(key)));
        }
        sb.append(String.format("\n>>>Parameter values (%d): ", internalParameterValues.size()));
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
