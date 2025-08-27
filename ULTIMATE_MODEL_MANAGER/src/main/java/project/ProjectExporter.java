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
import parameters.FixedExternalParameter;
import parameters.InternalParameter;
import parameters.LearnedExternalParameter;
import parameters.RangedExternalParameter;
import parameters.SynthesisObjective;
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
                depObj.put("name", dep.getNameInModel());
                depObj.put("modelId", dep.getSourceModel().getModelId());
                depObj.put("property", formatDefinition(dep.getDefinition()));
                dependencyObject.put(dep.getNameInModel(), depObj);
            }
            parametersObject.put("dependency", dependencyObject);

            // Handling environment parameters
            JSONObject externalObject = new JSONObject();
            for (ExternalParameter ep : model.getExternalParameters()) {
                JSONObject envObj = new JSONObject();
                envObj.put("name", ep.getNameInModel());
                if (ep instanceof RangedExternalParameter) {
                    envObj.put("type", "ranged");
                    envObj.put("rangedValues", new JSONArray(((RangedExternalParameter) ep).getValueOptions()));
                } else if (ep instanceof LearnedExternalParameter) {
                    envObj.put("type", ((LearnedExternalParameter)ep).getType());
                    envObj.put("value", ((LearnedExternalParameter) ep).getValueSource());
                } else if (ep instanceof FixedExternalParameter) {
                    envObj.put("type", "fixed");
                    envObj.put("value", ((FixedExternalParameter) ep).getValue());
                }
                // System.out.println("External Parameter: " + env.getName() + "\nType: " +
                // env.getType() + "\nValue: " + env.getValue());
                externalObject.put(ep.getNameInModel(), envObj);
            }
            parametersObject.put("environment", externalObject);

            // Handling internal parameters
            JSONObject internalObject = new JSONObject();
            for (InternalParameter internal : model.getInternalParameters()) {
                JSONObject ipNode = new JSONObject();
                ipNode.put("name", internal.getNameInModel());
                ipNode.put("min", internal.getMin());
                ipNode.put("max", internal.getMax());
                // ipNode.put("type", internal.getType());
                internalObject.put(internal.getNameInModel(), ipNode);
            }
            parametersObject.put("internal", internalObject);

            // the properties
            JSONArray propertiesArray = new JSONArray();
            for (Property p : model.getProperties()) {
                propertiesArray.put(formatDefinition(p.getDefinition()));
            }

            // synthesis objectives
            JSONArray synthesisPropertiesArray = new JSONArray();
            for (SynthesisObjective s : model.getSynthesisObjectives()) {
                synthesisPropertiesArray.put(s.getDefinition());
            }
            JSONObject synthesisNode = new JSONObject();
            synthesisNode.put("properties", synthesisPropertiesArray);

            modelObject.put("parameters", parametersObject);
            modelObject.put("properties", propertiesArray);
            modelObject.put("synthesis", synthesisNode);
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
            Files.write(Paths.get(location), content.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            Alerter.showErrorAlert("Project Not Saved!", e.getMessage());
            return;
        }
    }

    // FIXME this need to account for the difference between states, labels and
    // rewards as they are escaped differently
    /*
     * This method will extract labels in definitions and escape them correctly
     */
    private String formatDefinition(String definition) {
        String formattedDef = "";
        for (int i = 0; i < definition.length(); i++) {
            if (definition.charAt(i) == '\\') {
                ;
            } else if (definition.charAt(i) == '"') {
                formattedDef += '\\';
                formattedDef += definition.charAt(i);
            } else {
                formattedDef += definition.charAt(i);
            }
        }
        return formattedDef;
    }

}
