package project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import model.Model;
import utils.FileUtils;

public class Project {
	
	private Set<Model> models; // set of models in the project
	private ProjectImporter importer;
	//private ProjectExporter exporter;
	
	public Project(String projectPath) throws IOException {
		FileUtils.isUltimateFile(projectPath); // throws IOE if file is not an ultimate project file
		importer = new ProjectImporter(projectPath);
		models = importer.importProject();
    }
	
	public ArrayList<String> getModelIDs() {
		ArrayList<String> modelIDs = new ArrayList<String>();
		models.forEach(model -> {
			modelIDs.add(model.getModelId());
		});
		return modelIDs;
	}
	
	public void addModel(Model addModel) {
		models.forEach(model -> {
			if (model.getModelId().equals(addModel.getModelId())) {
				throw new IllegalArgumentException("Model already exists in project");
			}
		});
		models.add(addModel);
	}
}
	
