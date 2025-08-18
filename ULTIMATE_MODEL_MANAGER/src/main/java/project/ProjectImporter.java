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
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.application.Platform;

import org.json.JSONException;
import model.Model;
import parameters.DependencyParameter;
import parameters.ExternalParameter;
import parameters.FixedExternalParameter;
import parameters.InternalParameter;
import parameters.LearnedExternalParameter;
import parameters.RangedExternalParameter;
import utils.Alerter;

public class ProjectImporter {

	private String directory; // used when constructing models as they need the full path to their prism file
	private String projectFilePath;

	public ProjectImporter(String projectFilePath) {
		this.projectFilePath = projectFilePath;
		this.directory = getDirectory(projectFilePath);
	}

	public Set<Model> importProjectModels() throws IOException {
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
		for (Model model : models) {
			try {
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
			} catch (Exception e) {
				throw e;
			}
		}
		;
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
			HashSet<Model> models) throws IOException {
		switch (parameterType) {
			case "environment":
				// Deserialize environment parameters
				JSONObject environmentObject = parametersObject.optJSONObject("environment");
				ArrayList<String> brokenEParameterNames = new ArrayList<>();
				ArrayList<Exception> externalParameterImportExceptions = new ArrayList<>();

				// TODO: instantiate correct type of externalParameter based on 'type' from the
				// JSON
				if (environmentObject != null) {
					environmentObject.keySet().forEach(envName -> {
						JSONObject envObj = environmentObject.getJSONObject(envName);
						try {
							String type = envObj.getString("type");
							if (type.toLowerCase().equals("ranged")) {
								JSONArray rangedValues = envObj.getJSONArray("rangedValues");
								ArrayList<String> rangedValuesList = new ArrayList<>();

								for (int i = 0; i < rangedValues.length(); i++) {
									rangedValuesList.add(rangedValues.getString(i));
								}
								ExternalParameter envParam = new RangedExternalParameter(envName, rangedValuesList);
								model.addExternalParameter(envParam);
							} else if (type.toLowerCase().equals("fixed")) {
								String value = envObj.getString("value");
								ExternalParameter envParam = new FixedExternalParameter(envName, value);
								model.addExternalParameter(envParam);
							} else if (LearnedExternalParameter.LEARNED_PARAMETER_TYPE_OPTIONS
									.contains(type.toLowerCase())) {
								String valueSource = envObj.getString("value");
								ExternalParameter envParam = new LearnedExternalParameter(envName, type, valueSource);
								model.addExternalParameter(envParam);
							}

						} catch (Exception e) {
							externalParameterImportExceptions.add(e);
							brokenEParameterNames
									.add((envObj.has("name") ? envObj.getString("name")
											: "[unnamed external parameter]"));
						}
					});

					if (brokenEParameterNames.size() > 0) {
						throw new IOException(
								"Exception(s) occurred when importing the following external parameter(s) for '"
										+ model.getModelId() + "':\n\n"
										+ String.join(", ", brokenEParameterNames)
										+ "\n\nPlease make sure the file is properly formatted and each external parameter has the following fields:\n\ttype\n\trangedValues or value"
										+ "\nYou must reload the project for any changes to take effect."
										+ "\n\nThe exceptions were: \n"
										+ externalParameterImportExceptions.stream().map(Throwable::toString)
												.collect(Collectors.joining("\n")));
					}
				}
				break;
			case "dependency":
				// Deserialize dependency parameters
				JSONObject dependencyObject = parametersObject.optJSONObject("dependency");
				ArrayList<String> brokenDParameterNames = new ArrayList<>();
				if (dependencyObject != null) {
					dependencyObject.keySet().forEach(depName -> {
						JSONObject depObj = dependencyObject.getJSONObject(depName);
						try {
							String depId = depObj.getString("modelId");
							String depDefinition = depObj.getString("property");
							Model sourceModel = models.stream().filter(m -> m.getModelId().equals(depId)).findFirst()
									.orElse(null);
							if (sourceModel == null) {
								throw new RuntimeException("Tried to create a dependency parameter '" + depName
										+ "' for model '" + depId + "' which is dependent on model '" + depId
										+ "', but this model could not be found. Make sure all arguments are properly set.");
							} else {
								DependencyParameter depParam = new DependencyParameter(depName, sourceModel,
										depDefinition);
								model.addDependencyParameter(depParam);
							}
						} catch (Exception e) {
							brokenDParameterNames
									.add((depObj.has("name") ? depObj.getString("name")
											: "[unnamed dependency parameter]"));
						}
					});

					if (brokenDParameterNames.size() > 0) {
						throw new IOException(
								"Exception(s) occurred when importing the following dependency parameter(s) for '"
										+ model.getModelId() + "':\n\n"
										+ String.join(", ", brokenDParameterNames)
										+ "\n\nPlease make sure the file is properly formatted and each dependency parameter has the following fields:\n\tmodelId\n\tproperty"
										+ "\nYou must reload the project for any changes to take effect.");
					}
				}
				break;
			case "internal":
				// Deserialize internal parameters
				JSONObject internalParametersNode = parametersObject.optJSONObject("internal");
				ArrayList<String> brokenIParameterNames = new ArrayList<>();
				if (internalParametersNode != null) {
					// Iterate over subnodes of internalParametersNode
					internalParametersNode.keySet().forEach(subNodeKey -> {
						JSONObject ipn = internalParametersNode.getJSONObject(subNodeKey);
						try {
							InternalParameter internalParam = new InternalParameter(
									ipn.get("name").toString(),
									ipn.get("min").toString(),
									ipn.get("max").toString(),
									null);
							model.addInternalParameter(internalParam);
						} catch (JSONException e) {
							brokenIParameterNames
									.add((ipn.has("name") ? ipn.getString("name") : "[unnamed internal parameter]"));
						}
					});

					if (brokenIParameterNames.size() > 0) {
						throw new IOException(
								"Exception(s) occurred when importing the following internal parameter(s) for '"
										+ model.getModelId() + "':\n\n"
										+ String.join(", ", brokenIParameterNames)
										+ "\n\nPlease make sure the file is properly formatted and and each internal parameter has the following fields:\n\tname\n\ttype\n\tmin\n\tmax"
										+ "\n\nYou must reload the project for any changes to take effect.");
					}
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