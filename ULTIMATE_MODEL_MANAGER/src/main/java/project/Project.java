package project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import utils.FileUtils;

public class Project {
	
	private Set<Model> models; // set of models in the project
    private ObservableList<Model> observableModels;    // The observable list used for UI binding
    private Model currentModel;
	private String projectName;
	private ProjectImporter importer;
	//private ProjectExporter exporter;
	
	public Project(String projectPath) throws IOException {
		FileUtils.isUltimateFile(projectPath); // throws IOE if file is not an ultimate project file
		importer = new ProjectImporter(projectPath);
		models = importer.importProject();
		currentModel = models.iterator().next();
        // Initialize the observable list with the contents of the set
        observableModels = FXCollections.observableArrayList(models);
		this.projectName = FileUtils.removeUltimateFileExtension(projectPath);
    }
	
	public Project() {
		this.models = new HashSet<Model>();
        this.observableModels = FXCollections.observableArrayList();
		this.projectName = "untitled project";
    }
	
	public ArrayList<String> getModelIDs() {
		ArrayList<String> modelIDs = new ArrayList<String>();
		models.forEach(model -> {
			modelIDs.add(model.getModelId());
		});
		return modelIDs;
	}
	
    public ObservableList<Model> getObservableModels() {
        return observableModels;
    }
    
    public void addModel(Model addModel) {
        // Check if the model already exists using the set
        for (Model model : models) {
            if (model.getModelId().equals(addModel.getModelId())) {
                throw new IllegalArgumentException("Model already exists in project");
            }
        }
        // Add to the set
        models.add(addModel);
        // Update the observable list
        observableModels.add(addModel);
    }
    
    public void removeModel(Model removeModel) {
        // Remove from the set (by id comparison)
        models.removeIf(model -> model.getModelId().equals(removeModel.getModelId()));
        // Remove from the observable list
        observableModels.removeIf(model -> model.getModelId().equals(removeModel.getModelId()));
    }
	
	public String getProjectName() {
		return projectName;
	}
	
	public void setModel(Model model) {
		this.currentModel = model;
	}
	
	public Model getCurrentModel() {
		return currentModel;
	}
}
	
