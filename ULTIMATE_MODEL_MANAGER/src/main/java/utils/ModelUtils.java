package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

import model.persistent_objects.*;

import javafx.collections.ObservableList;

public class ModelUtils {
	
	// TODO refactor this awful method!!
	// TODO Need to save properties
	public static void saveModelsToFile(List<Model> models, String filePath) {
        JSONObject root = new JSONObject();
        JSONObject modelsObject = new JSONObject();

        for (Model model : models) {
            JSONObject modelObject = new JSONObject();
            modelObject.put("id", model.getModelId());
            modelObject.put("fileName", model.getFilePath());

            // Parameters object
            JSONObject parametersObject = new JSONObject();

            // Handling dependency parameters
            JSONObject dependencyObject = new JSONObject();
            for (DependencyParameter dep : model.getDependencyParameters()) {
                JSONObject depObj = new JSONObject();
                depObj.put("name", dep.getName());
                depObj.put("modelId", dep.getModelID());
                depObj.put("property", dep.getDefinition());
                dependencyObject.put(dep.getName(), depObj);  // Add dependency parameters to the object
            }
            parametersObject.put("dependency", dependencyObject);

            // Handling environment parameters
            JSONObject environmentObject = new JSONObject();
            for (EnvironmentParameter env : model.getEnvironmentParameters()) {
                JSONObject envObj = new JSONObject();
                envObj.put("name", env.getName());
                envObj.put("type", env.getCalculation());
                envObj.put("dataFile", env.getFilePath());
                environmentObject.put(env.getName(), envObj);  // Add environment parameters
            }
            parametersObject.put("environment", environmentObject);

            // Handling internal parameters (only name should be included)
            JSONObject internalObject = new JSONObject();
            for (InternalParameter internal : model.getInternalParameters()) {
                JSONObject internalObj = new JSONObject();
                internalObj.put("name", internal.getName());  // Only name for internal parameters
                internalObject.put(internal.getName(), internalObj);  // Add internal parameters
            }
            parametersObject.put("internal", internalObject);

            // Add the parameters to the model object
            modelObject.put("parameters", parametersObject);

            // Add model to the modelsObject
            modelsObject.put(model.getModelId(), modelObject);
        }

        // Wrap everything under "models"
        root.put("models", modelsObject);

        // Save the JSON to file
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(root.toString(4));  // Pretty print with indentation
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
    public static void parseAndInitializeModels(JSONObject root, ObservableList<Model> models) {
    	models.clear();
        JSONObject modelsObject = root.getJSONObject("models");
        modelsObject.keySet().forEach(modelId -> {
            Model model = initializeModel(modelsObject.getJSONObject(modelId));
            models.add(model);
        });
    }

    private static Model initializeModel(JSONObject modelJson) {
        String id = modelJson.getString("id");
        String filePath = modelJson.getString("fileName");
        Model model = new Model(id, filePath);

        JSONObject parametersObject = modelJson.getJSONObject("parameters");
        // Define the array of values
        String[] parameterTypes = {"environment", "dependency", "internal"};

        // Iterate over the array and call the method
        for (String parameterType : parameterTypes) {
            ParameterUtils.deserializeParameters(parametersObject, model, parameterType);
        }
        addUndefinedParameters(model);
        return model;
    }
    
    private static void addUndefinedParameters(Model model) {
       	PrismFileParser parser = new PrismFileParser();
        // Parse the model's file with PrismFileParser
        List<String> parsedParams = null;
		try {
			parsedParams = parser.parseFile(model.getFilePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String parsedParam : parsedParams) {
			if (!model.isParam(parsedParam)) {
				model.addUndefinedParameter(parsedParam);
			}
		}
    }
    
    public static String[] getModelIDs() {
    	SharedData context = SharedData.getInstance();
    	List<Model> models = context.getModels();
		String[] modelNames = new String[models.size()];
		for (int i = 0; i < models.size(); i++) {
			modelNames[i] = models.get(i).getModelId();
		}
		return modelNames;
    }
}