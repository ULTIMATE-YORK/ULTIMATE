package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import project.Project;
import sharedContext.SharedContext;

public class VerificationResult {

    private final HashMap<String, String> verifiedPropertyValues;
    private final HashMap<String, HashMap<String, String>> rangedParameterValues;
    private final String projectConfigKey;
    private final String cacheKey;
    private final String modelId;
    private final String propertyDefinition;

    public VerificationResult(String modelId, String propertyDefinition, 
            HashMap<String, String> verifiedPropertyValueMap) {
        this.verifiedPropertyValues = verifiedPropertyValueMap;
        this.propertyDefinition = propertyDefinition;
        this.modelId = modelId;
        this.projectConfigKey = SharedContext.getProject().generateParameterConfigurationKey();
        this.rangedParameterValues = SharedContext.getProject().getRangedParameterValuesPerModel();
        this.cacheKey = SharedContext.getProject().generateVerificationCacheKey(modelId, propertyDefinition);
    }

    public VerificationResult(VerificationRun run,
            HashMap<String, String> verifiedPropertyValueMap) {
        this.verifiedPropertyValues = verifiedPropertyValueMap;
        this.propertyDefinition = run.getPropertyDefinition();
        this.modelId = run.getModelId();
        this.projectConfigKey = SharedContext.getProject().generateParameterConfigurationKey();
        this.rangedParameterValues = SharedContext.getProject().getRangedParameterValuesPerModel();
        this.cacheKey = SharedContext.getProject().generateVerificationCacheKey(modelId, propertyDefinition);
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

    public String propertyDefinition() {
        return propertyDefinition;
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