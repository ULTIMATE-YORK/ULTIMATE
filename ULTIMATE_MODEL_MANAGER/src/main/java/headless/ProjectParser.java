package headless;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.json.JSONObject;

import model.persistent_objects.Model;
import utils.ParameterUtils;
import utils.PrismFileParser;

public class ProjectParser {
	
	public static ArrayList<Model> parse(String filePath) {
		
		try {
			File projectFile = new File(filePath);
			String content = new String(Files.readAllBytes(Paths.get(projectFile.toURI())));
			JSONObject root = new JSONObject(content);
			ArrayList<Model> models = parseAndInitializeModels(root);
			return models;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
    /**
     * Parses a JSON file and initializes the models list.
     * Ensures UI updates are performed on the JavaFX thread.
     *
     * @param root   The JSON object containing model data.
     * @param models The observable list to populate with models.
     */
    private static ArrayList<Model> parseAndInitializeModels(JSONObject root) {
    	ArrayList<Model> models = new ArrayList<Model>();
    	JSONObject modelsObject = root.getJSONObject("models");
        modelsObject.keySet().forEach(modelId -> {
                Model model = initializeModel(modelsObject.getJSONObject(modelId));
                models.add(model);
        });
        return models;
    }

    /**
     * Creates a Model instance from a JSON object.
     *
     * @param modelJson The JSON object containing model data.
     * @return The initialized Model object.
     */
    private static Model initializeModel(JSONObject modelJson) {
        String id = modelJson.getString("id");
        String filePath = modelJson.getString("fileName");
        Model model = new Model(id, filePath);

        JSONObject parametersObject = modelJson.getJSONObject("parameters");

        // Define the array of parameter types
        String[] parameterTypes = {"environment", "dependency", "internal"};
        for (String parameterType : parameterTypes) {
            ParameterUtils.deserializeParameters(parametersObject, model, parameterType);
        }

        addUndefinedParametersAsync(model);
        return model;
    }

    /**
     * Asynchronously adds undefined parameters to a model by parsing the model's file.
     * Ensures file operations run in the background.
     *
     * @param model The model to update with undefined parameters.
     */
    private static void addUndefinedParametersAsync(Model model) {
    	PrismFileParser parser = new PrismFileParser();
    	ArrayList<String> params = new ArrayList<String>();
        
    	try {
            params = parser.parseFile(model.getFilePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    	if (params != null) {
	        for (String parsedParam : params) {
	            if (!model.isParam(parsedParam)) {
	                model.addUndefinedParameter(parsedParam);
	            }
            }
        }
    }

}
