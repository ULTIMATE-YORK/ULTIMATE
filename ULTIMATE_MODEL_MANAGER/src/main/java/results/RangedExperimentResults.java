package results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RangedExperimentResults {

    private HashMap<String, ArrayList<String>> externalParameterValues;
    private HashMap<String, HashMap<String, Double>> experimentResults;

    // the results are stored in [key: config, value: numeric result]
    // externaParameterValues stores the labels of the externalParameters and their
    // intervals
    // everything needed to access the results is in externalParameterValues

    public RangedExperimentResults() {
        this.externalParameterValues = new HashMap<>();
        this.experimentResults = new HashMap<>();
    }

    public RangedExperimentResults(HashMap<String, ArrayList<String>> externalParameterValues) {
        this.externalParameterValues = externalParameterValues;
        this.experimentResults = new HashMap<>();
    }

    public RangedExperimentResults(HashMap<String, ArrayList<String>> externalParameterValues,
            HashMap<String, HashMap<String, Double>> results) {
        this.externalParameterValues = externalParameterValues;
        this.experimentResults = results;
    }

    public void setExternalParameterValues(HashMap<String, ArrayList<String>> externalParameterValues) {
        this.externalParameterValues = externalParameterValues;
    }

    public HashMap<String, ArrayList<String>> getExternalParameterValues() {
        return externalParameterValues;
    }

    public void addResult(HashMap<String, String> iterationConfiguration, HashMap<String, Double> iterationResults) {
        String experimentConfigurationKey = generateExperimentKey(iterationConfiguration);
        experimentResults.put(experimentConfigurationKey, iterationResults);
    }

    public void addResult(String experimentIterationKey, HashMap<String, Double> iterationResults) {
        experimentResults.put(experimentIterationKey, iterationResults);
    }

    public HashMap<String, Double> getIterationResult(HashMap<String, String> iterationConfiguration) {
        String experimentConfigurationKey = generateExperimentKey(iterationConfiguration);
        return experimentResults.get(experimentConfigurationKey);
    }

    public HashMap<String, Double> getIterationResult(String experimentIterationKey) {
        return experimentResults.get(experimentIterationKey);
    }

    public String generateExperimentKey(HashMap<String, String> experimentConfiguration) {

        ArrayList<String> keyConstructionArray = new ArrayList<>();

        for (Map.Entry<String, String> e : experimentConfiguration.entrySet()) {
            keyConstructionArray.add(e.getKey() + ":" + e.getValue() + "|");
        }

        String key = String.join(",", keyConstructionArray);
        return key;
    }

}
