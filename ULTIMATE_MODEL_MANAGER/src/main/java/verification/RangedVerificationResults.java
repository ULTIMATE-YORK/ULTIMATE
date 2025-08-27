package verification;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RangedVerificationResults {

    private HashMap<String, String> externalParametersValues;
    private HashMap<String, String> results;

    public RangedVerificationResults(HashMap<String, String> externalParametersValues,
            HashMap<String, String> results) {
        this.externalParametersValues = externalParametersValues;
        this.results = results;
    }

    public HashMap<String, String> getExternalParametersValues() {
        return externalParametersValues;
    }

    public HashMap<String, String> getResults() {
        return results;
    }

    public boolean matchesExternalParameterValues(HashMap<String, String> externalParameterValues) {
        return externalParameterValues.equals(this.externalParametersValues);
    }

    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(externalParametersValues.values().stream().collect(Collectors.joining("|")));
        sb.append(": ");
        sb.append(results.values().stream().collect(Collectors.joining("|")));
        return sb.toString();
    }

}
