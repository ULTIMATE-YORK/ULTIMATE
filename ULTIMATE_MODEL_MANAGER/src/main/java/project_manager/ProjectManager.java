package project_manager;

import java.nio.file.Path;
import java.util.NoSuchElementException;

import exceptions.DuplicateModelException;
import exceptions.InvalidPrismFile;
import javafx.collections.ObservableList;
import project_manager.import_export.ProjectExporter;
import project_manager.import_export.ProjectImporter;

public class ProjectManager {
	
	private ObservableList<Model> projectModels; // ObservableList allows for binding to this data structure for event-driven UI updates
	private ProjectImporter projectImporter;
	private ProjectExporter projectExporter;
	private boolean isSaved; // will only be true when a non-blank project has been saved and no changes have been made since
	
	/*
	 * This constructor initialises the ProjectManager with an empty list of models.
	 */
	public ProjectManager() {
		
	}
	
	/*
	 * This constructor initialises the ProjectManager with a specified project file.
	 * 
	 * @param projectFile The path to the project file.
	 */
	public ProjectManager(Path projectFile) {
		
	}
	
	/*
	 * This method will instantiate a new Model object and add it to the projectModels list.
	 * 
	 * @param prismFile The path to the Prism file for the Model to be added to the project.
	 * 
	 * @return true if the Model was added successfully, false otherwise.
	 * 
	 * @throws DuplicateModelException if a Model with the same ID already exists in the projectModels list.
	 * @throws InvalidPrismFile if the Prism file is invalid.
	 */
	public boolean addModel(Path prismFile) throws DuplicateModelException, InvalidPrismFile {
		return true;
	}
	
	/*
	 * This method will remove a Model object from the projectModels list.
	 * 
	 * @param model The Model object to be removed from the projectModels list.
	 * 
	 * @return true if the Model was removed successfully, false otherwise.
	 * 
	 * @throws NoSuchElementException if the Model object is not found in the projectModels list.
	 */
	public boolean removeModel(Model model) throws NoSuchElementException {
		return true;
	}
	
	/*
	 * This method will add a DependencyParameter to the specified Model object.
	 * 
	 * @param name The name of the DependencyParameter to be added.
	 * @param property The Property object associated with the DependencyParameter.
	 * @param modelID The Model object that the DependencyParameter is associated with.
	 * @param addToModel The Model object to which the DependencyParameter will be added.
	 * 
	 * @return true if the DependencyParameter was added successfully, false otherwise.
	 */
	public boolean addDependencyParameter(String name, Property property, Model modelID, Model addToModel) {
		return true;
	}
	
	/*
	 * This method will remove a DependencyParameter from the specified Model object.
	 * 
	 * @param name The name of the DependencyParameter to be removed.
	 * @param modelID The Model object that the DependencyParameter is associated with.
	 * 
	 * @return true if the DependencyParameter was removed successfully, false otherwise.
	 * 
	 * @throws NoSuchElementException if the DependencyParameter is not found in the specified Model object.
	 */
	public boolean removeDependencyParameter(String name, Model modelID) throws NoSuchElementException {
		return true;
	}
	
	/*
	 * This method will edit a DependencyParameter in the specified Model object.
	 * 
	 * @param name The name of the DependencyParameter to be edited.
	 * @param property The Property object associated with the DependencyParameter.
	 * @param modelID The Model object that the DependencyParameter is associated with.
	 * @param editToModel The Model object to which the DependencyParameter will be edited.
	 * 
	 * @return true if the DependencyParameter was edited successfully, false otherwise.
	 */
	public boolean editDependencyParameter(String name, Property property, Model modelID, Model editToModel) {
		return true;
	}
	
	/*
	 * This method will add an ExternalParameter to the specified Model object.
	 * 
	 * @param name The name of the ExternalParameter to be added.
	 * @param type The type of the ExternalParameter.
	 * @param source The source of the ExternalParameter data file if there is one
	 * @param value The value of the ExternalParameter.
	 * @param addToModel The Model object that the ExternalParameter is to be added to.
	 * 
	 * @return true if the ExternalParameter was added successfully, false otherwise.
	 */
	public boolean addExternalParameter(String name, String type, Path source, Double value, Model addToModel) {
		return true;
	}
	
	/*
	 * This method will remove an ExternalParameter from the specified Model object.
	 * 
	 * @param name The name of the ExternalParameter to be removed.
	 * @param modelID The Model object that the ExternalParameter is associate with.
	 * 
	 * @return true if the ExternalParameter was removed successfully, false otherwise.
	 * 	
	 * @throws NoSuchElementException if the ExternalParameter is not found in the specified Model object.
	 */
	public boolean removeExternalParameter(String name, Model modelID) throws NoSuchElementException {
		return true;
    }
	
	/*
	 * This method will edit an ExternalParameter in the specified Model object.
	 * 
	 * @param name The name of the ExternalParameter to be edited.
	 * @param type The type of the ExternalParameter.
	 * @param source The source of the ExternalParameter data file if there is one
	 * @param value The value of the ExternalParameter.
	 * @param editToModel The Model object to which the ExternalParameter will be edited.
	 * 
	 * @return true if the ExternalParameter was edited successfully, false otherwise.
	 */
	public boolean editExternalParameter(String name, String type, Path source, Double value, Model editToModel) {
		return true;
    }
	
	/*
	 * This method will add an InternalParameter to the specified Model object.
	 * 
	 * @param name The name of the InternalParameter to be added.
	 * @param type The type of the InternalParameter.
	 * @param addToModel The Model object that the InternalParameter is to be added to.
	 * 
	 * @return true if the InternalParameter was added successfully, false otherwise.
	 */
	public boolean addInternalParameter(String name, String type, Model addToModel) {
		return true;
	}
	
	/*
	 * This method will remove an InternalParameter from the specified Model object.
	 * 
	 * @param name The name of the InternalParameter to be removed.
	 * @param modelID The Model object that the InternalParameter is associated with.
	 * 
	 * @return true if the InternalParameter was removed successfully, false
	 * otherwise.
	 * 
	 * @throws NoSuchElementException if the InternalParameter is not found in the
	 * specified Model object.
	 */
	public boolean removeInternalParameter(String name, Model modelID) throws NoSuchElementException {
		return true;
	}
	
	/*
	 * This method will edit an InternalParameter in the specified Model object.
	 * 
	 * @param name The name of the InternalParameter to be edited.
	 * @param type The type of the InternalParameter.
	 * @param editToModel The Model object to which the InternalParameter will be
	 * edited.
	 * 
	 * @return true if the InternalParameter was edited successfully, false
	 * otherwise.
	 */
	public boolean editInternalParameter(String name, String type, Model editToModel) {
		return true;
	}
	
	/*
	 * This method will add a Property to the specified Model object.
	 * 
	 * @param definition The definition of the Property to be added.
	 * @param addToModel The Model object that the Property is to be added to.
	 * 
	 * @return true if the Property was added successfully, false otherwise.
	 */
	public boolean addProperty(String definition, Model addToModel) {
		return true;
	}
	
	/*
	 * This method will remove a Property from the specified Model object.
	 * 
	 * @param definition The definition of the Property to be removed.
	 * @param modelID The Model object that the Property is associated with.
	 * 
	 * @return true if the Property was removed successfully, false otherwise.
	 * 
	 * @throws NoSuchElementException if the Property is not found in the specified Model object.
	 */
	public boolean removeProperty(String definition, Model modelID) throws NoSuchElementException {
		return true;
	}
	
	/*
	 * This method will edit a Property in the specified Model object.
	 * 
	 * @param definition The definition of the Property to be edited.
	 * @param editToModel The Model object to which the Property will be edited.
	 * 
	 * @return true if the Property was edited successfully, false otherwise.
	 */
	public boolean editProperty(String definition, Model editToModel) {
		return true;
	}
	
	/*
	 * This method will import a project from the specified file path.
	 * 
	 * @param filePath The path to the file to be imported.
	 * 
	 * @return true if the project was imported successfully, false otherwise.
	 */
	public boolean importProject(Path filePath) {
		return true;
	}
	
	/*
	 * This method will export the project to the specified file path.
	 * 
	 * @param filePath The path to the file to be exported to.
	 * 
	 * @return true if the project was exported successfully, false otherwise.
	 */
	public boolean exportProject(Path filePath) {
		return true;
	}
	
	// This method will return true if the project is blank (has no models), false otherwise.
	private boolean isBlank() {
		return true;
	}
}
