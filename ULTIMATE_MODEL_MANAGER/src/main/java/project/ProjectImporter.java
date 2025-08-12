package project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import model.Model;
import parameters.DependencyParameter;
import parameters.ExternalParameter;
import parameters.InternalParameter;
import utils.Alerter;

public class ProjectImporter {

	private String directory; // used when constructing models as they need the full path to their prism file
	private String projectFilePath;

	public ProjectImporter(String projectFilePath) {
		this.projectFilePath = projectFilePath;
		this.directory = getDirectory(projectFilePath);
	}

	public Set<Model> importProject() throws IOException {
		HashSet<Model> models = extractModels();
		return models;
	}

	/*
	 * Extracts the JSON objects from the project file
	 */
	private Map<String, JSONObject> extractJSONObjects() throws IOException {
		File projectFile = new File(projectFilePath);
		String content = new String(Files.readAllBytes(Paths.get(projectFile.toURI())));
		JSONObject projectJSON = new JSONObject(content);
		JSONObject modelsObject = projectJSON.getJSONObject("models");
		Map<String, JSONObject> hashMap = new HashMap<>();
		modelsObject.keySet().forEach(model -> hashMap.put(model, modelsObject.getJSONObject(model)));
		return hashMap;
	}

	/*
	 * Extracts the models from the project
	 */
	private HashSet<Model> extractModels() throws IOException {
		HashSet<Model> models = new HashSet<Model>();
		Map<String, JSONObject> modelObjects = extractJSONObjects();

		// create the models
		for (String modelId : modelObjects.keySet()) {
			// FIXME is this OS-dependent?
			try {
				Model model = new Model(directory + "/" + modelObjects.get(modelId).getString("fileName"));
				models.add(model);
			} catch (IOException e) {
				Alerter.showErrorAlert("Project Error", "The model with ID {" + modelId
						+ "} was not created!\nMake sure the model file is in the same directory as the project file.");
			}
		}

		// Initialise the models parameters
		models.forEach(model -> {
			JSONObject parametersObject = modelObjects.get(model.getModelId()).getJSONObject("parameters");
			// Define the array of parameter types
			String[] parameterTypes = { "environment", "dependency", "internal" };
			for (String parameterType : parameterTypes) {
				deserializeParameters(parametersObject, model, parameterType, models);
			}
			model.addUncategorisedParametersFromFile();
			// add the properties
			JSONArray properties = modelObjects.get(model.getModelId()).getJSONArray("properties");
			for (int i = 0; i < properties.length(); i++) {
				model.addProperty(properties.getString(i));
			}
		});
		return models;
	}

	/*
	 * Deserializes the parameters of the model
	 * 
	 * @param parametersObject the JSON object containing the parameters
	 * 
	 * @param model the model to add the parameters to
	 * 
	 * @param parameterType the type of parameter to deserialize
	 * 
	 * @param models the set of models in the project
	 */
	private void deserializeParameters(JSONObject parametersObject, Model model, String parameterType,
			HashSet<Model> models) {
		switch (parameterType) {
			case "environment":
				// Deserialize environment parameters
				JSONObject environmentObject = parametersObject.optJSONObject("environment");
				if (environmentObject != null) {
					environmentObject.keySet().forEach(envName -> {
						JSONObject envObj = environmentObject.getJSONObject(envName);
						String type = envObj.getString("type");
						if (type.equals("Ranged")) {
							JSONArray rangedValues = envObj.getJSONArray("rangedValues");
							ArrayList<Double> rangedValuesList = new ArrayList<>();

							for (int i = 0; i < rangedValues.length(); i++) {
								rangedValuesList.add(rangedValues.getDouble(i));
							}
							try {
								ExternalParameter envParam = new ExternalParameter(envName, type, rangedValuesList);
								model.addExternalParameter(envParam);
							} catch (IOException e) {
							}
						} else {
							String value = envObj.getString("value");
							try {
								ExternalParameter envParam = new ExternalParameter(envName, type, value);
								model.addExternalParameter(envParam);
							} catch (IOException e) {
							}
						}
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
						models.forEach(modelo -> {
							if (modelo.getModelId().equals(depId)) {
								// FIXME: what if a model is not in the project?
								DependencyParameter depParam = new DependencyParameter(depName, modelo, depDefinition);
								model.addDependencyParameter(depParam);
							}
						});
					});
				}
				break;
			case "internal":
				// Deserialize internal parameters
				JSONObject internalParametersNode = parametersObject.optJSONObject("internal");
				if (internalParametersNode != null) {

					// Iterate over subnodes of internalParametersNode
					internalParametersNode.keySet().forEach(subNodeKey -> {
						JSONObject ipn = internalParametersNode.getJSONObject(subNodeKey);
						InternalParameter internalParam = new InternalParameter(
							ipn.get("name").toString(),
							ipn.get("type").toString(),
							ipn.get("min").toString(),
							ipn.get("max").toString(),
							null
							);
						model.addInternalParameter(internalParam);
					});

				}
				break;
		}
	}

	/*
	 * Gets the directory of the project
	 */
	private String getDirectory(String projectPath) {
		Path path = Paths.get(projectPath);
		return path.toAbsolutePath().getParent().toString(); // Get directory
	}

}