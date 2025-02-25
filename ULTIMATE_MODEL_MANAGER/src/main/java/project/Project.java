package project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import sharedContext.SharedContext;
import utils.Alerter;
import utils.FileUtils;

public class Project {
	
	private Set<Model> models; // set of models in the project
    private ObservableList<Model> observableModels;    // The observable list used for UI binding
    private ObjectProperty<Model> currentModel; // Observable property for current model
	private String projectName;
	private ProjectImporter importer;
	private ProjectExporter exporter;
	private String saveLocation; // set when a project has been saved as, used for subsequent saves
    private SharedContext sharedContext = SharedContext.getInstance();
	
	public Project(String projectPath) throws IOException {
		FileUtils.isUltimateFile(projectPath); // throws IOE if file is not an ultimate project file
		importer = new ProjectImporter(projectPath);
		models = importer.importProject();
        // Initialize currentModel property (first model in the set or null)
        currentModel = new SimpleObjectProperty<>(models.isEmpty() ? null : models.iterator().next());
        // Initialize the observable list with the contents of the set
        observableModels = FXCollections.observableArrayList(models);
		this.projectName = FileUtils.removeUltimateFileExtension(projectPath);
        sharedContext.setProject(this);
		sharedContext.getMainStage().setTitle("Ultimate Stochastic World Model Manager: " + projectName);
    }
	
	public Project() {
		this.models = new HashSet<Model>();
        this.observableModels = FXCollections.observableArrayList();
        this.currentModel = new SimpleObjectProperty<>(null);
		this.projectName = "untitled";
        sharedContext.setProject(this);
		sharedContext.getMainStage().setTitle("Ultimate Stochastic World Model Manager: " + projectName);
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
            	Alerter.showErrorAlert("Model Already Exists!", "The model could not be added as it exists in the project.");
            	return;
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
	
	public Set<Model> getModels() {
		return this.models;
	}
	
    // Getter for currentModel property (for binding)
    public ObjectProperty<Model> currentModelProperty() {
        return currentModel;
    }

    public void setCurrentModel(Model model) {
        this.currentModel.set(model);
    }

    public Model getCurrentModel() {
        return currentModel.get();
    }
    
    public void save(String saveLocation) {
    	exporter = new ProjectExporter(this);
    	exporter.saveExport(saveLocation);
    }
    
    public void save() {
    	exporter = new ProjectExporter(this);
    	exporter.saveExport(saveLocation);
    }
    
    public void setSaveLocation(String location) {
    	this.saveLocation = location;
    }
    
    public String getSaveLocation() {
    	return this.saveLocation;
    }
}
	
