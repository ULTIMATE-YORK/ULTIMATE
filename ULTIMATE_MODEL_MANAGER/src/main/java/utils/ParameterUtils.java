package utils;

import model.persistent_objects.*;

import org.json.JSONObject;

/**
 * Utility class for deserializing model parameters from JSON.
 */

public class ParameterUtils {

    public static void deserializeParameters(JSONObject parametersObject, Model model, String parameterType) {
		switch (parameterType) {
		case "environment":
            // Deserialize environment parameters
            JSONObject environmentObject = parametersObject.optJSONObject("environment");
            if (environmentObject != null) {
                environmentObject.keySet().forEach(envName -> {
                    JSONObject envObj = environmentObject.getJSONObject(envName);
                    String filePathEnv = envObj.getString("dataFile");
                    String calculation = envObj.getString("type");
                    model.addEnvironmentParameter(envName, filePathEnv, calculation);
                });
            }
			break;
		case "dependency":
            // Deserialize dependency parameters
            JSONObject dependencyObject = parametersObject.optJSONObject("dependency");
            if (dependencyObject != null) {
                dependencyObject.keySet().forEach(depName -> {
                    JSONObject depObj = dependencyObject.getJSONObject(depName);
                    String depId = depObj.getString("modelId");
                    String depDefinition = depObj.getString("property");
                    model.addDependencyParameter(depName, depId, depDefinition);
                });
            }
			break;
		case "internal":
            // Deserialize internal parameters
            JSONObject internalObject = parametersObject.optJSONObject("internal");
            if (internalObject != null) {
                internalObject.keySet().forEach(internalName -> {
                    model.addInternalParameter(internalName);
                });
            }
			break;
		}
    }
}