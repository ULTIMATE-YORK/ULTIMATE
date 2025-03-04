package project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import model.Model;
import parameters.DependencyParameter;
import parameters.EnvironmentParameter;
import parameters.InternalParameter;
import utils.Alerter;
import utils.FileUtils;

public class ProjectExporter {
	
	private Set<Model> models;
	private JSONObject exportObject;
	
	public ProjectExporter(Project project) {
		this.models = project.getModels();
		export();
	}
	
	private JSONObject export() {
		JSONObject root = new JSONObject();
        JSONObject modelsObject = new JSONObject();

		for (Model model : models) {
			JSONObject modelObject = new JSONObject();
            modelObject.put("id", model.getModelId());
            try {
				modelObject.put("fileName", FileUtils.removeFullPathPrism(model.getFilePath()));
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

            // Parameters object
            JSONObject parametersObject = new JSONObject();

            // Handling dependency parameters
            JSONObject dependencyObject = new JSONObject();
            for (DependencyParameter dep : model.getDependencyParameters()) {
                JSONObject depObj = new JSONObject();
                depObj.put("name", dep.getName());
                depObj.put("modelId", dep.getModel().getModelId());
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
        this.exportObject = root;
		return root;
	}
	
	public void saveExport(String location) {
		try {
			String content = this.exportObject.toString(4);
			Files.write(Paths.get(location), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    	} catch (IOException e) {
    	Alerter.showErrorAlert("Project Not Saved!", e.getMessage());
    	return;    
    	}
	}

}
