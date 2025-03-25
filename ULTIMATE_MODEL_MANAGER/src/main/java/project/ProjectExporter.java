package project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import model.Model;
import parameters.DependencyParameter;
import parameters.ExternalParameter;
import parameters.InternalParameter;
import property.Property;
import utils.Alerter;
import utils.FileUtils;

public class ProjectExporter {
	
	private Set<Model> models;
	private JSONObject exportObject;
	Project project;
	
	public ProjectExporter(Project project) {
		this.project = project;
	}
	
	private JSONObject export() {
		models = project.getModels();
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
                depObj.put("property", formatDefinition(dep.getDefinition()));
                dependencyObject.put(dep.getName(), depObj);
            }
            parametersObject.put("dependency", dependencyObject);

            // Handling environment parameters
            JSONObject environmentObject = new JSONObject();
            for (ExternalParameter env : model.getExternalParameters()) {
                JSONObject envObj = new JSONObject();
                envObj.put("name", env.getName());
                envObj.put("type", env.getType());
                envObj.put("value", env.getValue());
                //System.out.println("External Parameter: " + env.getName() + "\nType: " + env.getType() + "\nValue: " + env.getValue());
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
            
            // the properties
            JSONArray propertiesArray = new JSONArray();
            for (Property p : model.getProperties()) {
            	propertiesArray.put(formatDefinition(p.getProperty()));
            }

            modelObject.put("parameters", parametersObject);
            modelObject.put("properties", propertiesArray);
            modelsObject.put(model.getModelId(), modelObject);
        }

        root.put("models", modelsObject);
        this.exportObject = root;
		return root;
	}
	
	public void saveExport(String location) {
		export();
		try {
			String content = this.exportObject.toString(4);
			Files.write(Paths.get(location), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    	} catch (IOException e) {
    	Alerter.showErrorAlert("Project Not Saved!", e.getMessage());
    	return;    
    	}
	}
	
	// FIXME this need to account for the difference between states, labels and rewards as they are escaped differently
    /*
     * This method will extract labels in definitions and escape them correctly
     */
    private String formatDefinition(String definition) {
    	String formattedDef = "";
    	for (int i = 0; i < definition.length(); i++) {
			if (definition.charAt(i) == '\\') {
				;
			}
			else if (definition.charAt(i) == '"') {
    			formattedDef += '\\';
    			formattedDef += definition.charAt(i);
    		}
    		else {
    			formattedDef += definition.charAt(i);
    		}
    	}
    	return formattedDef;
	}

}
