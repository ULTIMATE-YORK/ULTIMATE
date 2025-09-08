package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import project.Project;

public class VerificationResult {

    private final HashMap<String, String> verifiedPropertyValues;
    private final HashMap<String, HashMap<String, String>> rangedParameterValues;
    private final String projectConfigKey;
    private final String cacheKey;
    private final String modelId;
    private final String propertyDefinition;
    private final VerificationRun parentRun;

    public VerificationResult(VerificationRun run,
            HashMap<String, String> verifiedPropertyValueMap, Project project) {

        this.parentRun = run;
        this.verifiedPropertyValues = verifiedPropertyValueMap;
        this.propertyDefinition = run.getPropertyDefinition();
        this.modelId = run.getModelId();
        this.projectConfigKey = run.getProjectConfig();
        this.rangedParameterValues = project.getRangedParameterValuesPerModel();
        this.cacheKey = project.generateVerificationCacheKey(modelId, propertyDefinition);
    }

    public HashMap<String, String> getVerifiedPropertyValueMap() {
        return verifiedPropertyValues;
    }

    public List<String> getVerifiedPropertyDefinitions() {
        return new ArrayList<>(verifiedPropertyValues.values());
    }

    public String getProjectConfigKey() {
        return projectConfigKey;
    }

    public String getTargetModelId() {
        return modelId;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public String propertyDefinition() {
        return propertyDefinition;
    }

    public VerificationRun getParentRun() {
        return parentRun;
    }

    public HashMap<String, HashMap<String, String>> getRangedParameterValues() {
        return rangedParameterValues;
    }

    public String getDisplayString() {
        return String.format("Verification result:\n"
                + ">>> Ranged parameter values:\n\t%s"
                + "\n>>> Verified property values:\n\t%s",
                rangedParameterValues.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.joining("\n\t")),
                verifiedPropertyValues.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.joining("\n\t")));
    }

}