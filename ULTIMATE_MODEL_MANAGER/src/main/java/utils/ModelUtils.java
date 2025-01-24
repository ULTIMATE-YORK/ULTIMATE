package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;
import model.persistent_objects.*;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/**
 * Utility class for managing models, including saving, loading, and initializing.
 * Provides asynchronous operations to avoid UI blocking.
 */
public class ModelUtils {

    /**
     * Asynchronously saves a list of models to a file.
     * Ensures that the file operations do not block the UI thread.
     *
     * @param models   The list of models to save.
     * @param filePath The path to save the models as a JSON file.
     * @param callback Callback to notify success or failure.
     */
    public static void saveModelsToFileAsync(List<Model> models, String filePath, FileSaveCallback callback) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
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
                        dependencyObject.put(dep.getName(), depObj);
                    }
                    parametersObject.put("dependency", dependencyObject);

                    // Handling environment parameters
                    JSONObject environmentObject = new JSONObject();
                    for (EnvironmentParameter env : model.getEnvironmentParameters()) {
                        JSONObject envObj = new JSONObject();
                        envObj.put("name", env.getName());
                        envObj.put("type", env.getCalculation());
                        envObj.put("dataFile", env.getFilePath());
                        environmentObject.put(env.getName(), envObj);
                    }
                    parametersObject.put("environment", environmentObject);

                    // Handling internal parameters
                    JSONObject internalObject = new JSONObject();
                    for (InternalParameter internal : model.getInternalParameters()) {
                        JSONObject internalObj = new JSONObject();
                        internalObj.put("name", internal.getName());
                        internalObject.put(internal.getName(), internalObj);
                    }
                    parametersObject.put("internal", internalObject);

                    modelObject.put("parameters", parametersObject);
                    modelsObject.put(model.getModelId(), modelObject);
                }

                root.put("models", modelsObject);

                // Write the JSON object to the specified file
                try (FileWriter file = new FileWriter(filePath)) {
                    file.write(root.toString(4));  // Pretty print with indentation
                    file.flush();
                }

                return null;
            }
        };

        task.setOnSucceeded(event -> callback.onSuccess());
        task.setOnFailed(event -> callback.onError(task.getException()));

        new Thread(task).start();  // Run in a background thread
    }

    /**
     * Parses a JSON file and initializes the models list.
     * Ensures UI updates are performed on the JavaFX thread.
     *
     * @param root   The JSON object containing model data.
     * @param models The observable list to populate with models.
     */
    public static void parseAndInitializeModels(JSONObject root, ObservableList<Model> models) {
        Platform.runLater(() -> {
            models.clear();
            JSONObject modelsObject = root.getJSONObject("models");
            modelsObject.keySet().forEach(modelId -> {
                Model model = initializeModel(modelsObject.getJSONObject(modelId));
                models.add(model);
            });
        });
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
        CompletableFuture.supplyAsync(() -> {
            PrismFileParser parser = new PrismFileParser();
            try {
                return parser.parseFile(model.getFilePath());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(parsedParams -> {
            if (parsedParams != null) {
                Platform.runLater(() -> {
                    for (String parsedParam : parsedParams) {
                        if (!model.isParam(parsedParam)) {
                            model.addUndefinedParameter(parsedParam);
                        }
                    }
                });
            }
        });
    }

    /**
     * Retrieves model IDs from the shared data context.
     *
     * @return An array of model IDs.
     */
    public static String[] getModelIDs() {
        SharedData context = SharedData.getInstance();
        List<Model> models = context.getModels();
        String[] modelNames = new String[models.size()];
        for (int i = 0; i < models.size(); i++) {
            modelNames[i] = models.get(i).getModelId();
        }
        return modelNames;
    }

    /**
     * Callback interface for file saving operations.
     */
    public interface FileSaveCallback {
        void onSuccess();
        void onError(Throwable e);
    }
}