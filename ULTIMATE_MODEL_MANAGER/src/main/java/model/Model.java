package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import parameters.DependencyParameter;
import parameters.ExternalParameter;
import parameters.InternalParameter;
import parameters.UncategorisedParameter;
import property.Property;
import utils.Alerter;
import utils.FileUtils;
import utils.PrismFileParser;

/**
 * Represents a model in the system.
 * This class stores information about a model, including its unique ID, file path,
 * and various types of parameters associated with the model (e.g., dependency parameters, environment parameters).
 */
public class Model {
    private String modelId; // Unique identifier for the model
    private String filePath; // Path to the model's file
    //private String propertiesFile; // file of properties list
    private ObservableList<DependencyParameter> dependencyParameters; // List of dependency parameters
    private ObservableList<ExternalParameter> externalParameters; // List of environment parameters
    private List<InternalParameter> internalParameters; // List of internal parameters
    private ObservableList<UncategorisedParameter> uncategorisedParameters; // List of undefined parameters
    private ObservableList<Property> properties;
    
    /**
     * Constructor to initialise a new Model object.
     *
     * @param filePath the file path of the model
     * @throws IOException if the file is not a valid PRISM model
     */
    public Model(String filePath) throws IOException {
        this.modelId = FileUtils.removePrismFileExtension(filePath); // will throw an error if the file is not prism file
        this.filePath = filePath;
        this.dependencyParameters = FXCollections.observableArrayList();
        this.externalParameters = FXCollections.observableArrayList();
        this.internalParameters = new ArrayList<>();
        this.uncategorisedParameters = FXCollections.observableArrayList();
        //addUncategorisedParametersFromFile();
        
       this.properties = FXCollections.observableArrayList();
    }
    
    public void addProperty(String newProp) {
    	// check the property is novel
    	for (Property p : properties) {
    		if (p.getProperty().equals(newProp)) {
    			Alerter.showWarningAlert("Property Already Exists", "The property could not be added to the model");
    			return;
    		}
    	}
		//Alerter.showInfoAlert("SUCCESS", "The property was added");
		properties.add(new Property(newProp));
    }
    
    public void removeProperty(Property remove) {
    	boolean removed = properties.remove(remove);
    	if (removed) {
    		Alerter.showInfoAlert("SUCCESS", "The property was removed");
    	}
    	else {
    		Alerter.showWarningAlert("FAILED", "The property could not be removed!");
    	}
    }
    
    public ObservableList<Property> getProperties() {
    	return properties;
    }

    /**
     * Adds a dependency parameter to the model.
     * 
     * @param parameter the dependency parameter to add
     */   
	public void addDependencyParameter(DependencyParameter parameter) {
		dependencyParameters.add(parameter);
	}
	
	/*
	 * Adds a list of dependency parameters to the model.
	 * 
	 * @param parameters the list of dependency parameters to add
	 */
	public void setDependencyParameters(ObservableList<DependencyParameter> parameters) {
		dependencyParameters = parameters;
	}
	
    /**
     * Adds an environment parameter to the model.
     * 
     * @param parameter the dependency parameter to add
     */
	public void addExternalParameter(ExternalParameter parameter) {
		externalParameters.add(parameter);
	}
	
	/*
	 * Adds a list of environment parameters to the model.
	 * 
	 * @param parameters the list of environment parameters to add
	 */
	public void setExternalParameters(ObservableList<ExternalParameter> parameters) {
		externalParameters = parameters;
	}
	
    /**
     * Adds an internal parameter to the model.
     * 
     * @param parameter the dependency parameter to add
     */
	public void addInternalParameter(InternalParameter parameter) {
		internalParameters.add(parameter);
	}
	
	/*
	 * Adds a list of internal parameters to the model.
	 * 
	 * @param parameters the list of internal parameters to add
	 */
	public void setInternalParameters(List<InternalParameter> parameters) {
		internalParameters = parameters;
	}
	
    /**
     * Adds an uncategorised parameter to the model.
     * 
     * @param parameter the uncategorised parameter to add
     */
	public void addUncategorisedParameter(UncategorisedParameter parameter) {
		uncategorisedParameters.add(parameter);
	}
	
	/*
	 * Adds a list of uncategorised parameters to the model.
	 * 
	 * @param parameters the list of uncategorised parameters to add
	 */
	public void setUncategorisedParameters(ObservableList<UncategorisedParameter> parameters) {
		uncategorisedParameters = parameters;
	}

    /**
     * Gets the model's unique identifier.
     * 
     * @return the model ID
     */
    public String getModelId() {
        return this.modelId;
    }

    /**
     * Gets the model's file path.
     * 
     * @return the file path
     */
    public String getFilePath() {
        return this.filePath;
    }

    /**
     * Sets the model's file path.
     * 
     * @param filePath2 the new file path
     * @throws IOException 
     */
    public void setFilePath(String filePath) throws IOException {
    	if (FileUtils.isPrismFile(filePath)) {
    		this.filePath = filePath;
    	}
    }
    
    /**
     * Gets the list of dependency parameters associated with the model.
     * 
     * @return the list of dependency parameters
     */
    public ObservableList<DependencyParameter> getDependencyParameters() {
        return dependencyParameters;
    }
    
    /**
     * Gets the list of environment parameters associated with the model.
     * 
     * @return the list of environment parameters
     */
    public ObservableList<ExternalParameter> getExternalParameters() {
        return externalParameters;
    }
    
    /**
     * Gets the list of internal parameters associated with the model.
     * 
     * @return the list of internal parameters
     */
    public List<InternalParameter> getInternalParameters() {
        return internalParameters;
    }
    
    /**
     * Gets the list of uncategorised parameters associated with the model.
     * 
     * @return the list of uncategorised parameters
     */
    public ObservableList<UncategorisedParameter> getUncategorisedParameters() {
        return uncategorisedParameters;
    }
    
	/*
	 * Adds the uncategorised parameters to the model
	 */
    public void addUncategorisedParametersFromFile() {
        PrismFileParser parser = new PrismFileParser();
        try {
            List<String> params = parser.parseFile(this.getFilePath());
            for (String parsedParam : params) {
                boolean exists = dependencyParameters.stream()
                    .anyMatch(dp -> dp.getName().equals(parsedParam));
                if (!exists) {
                    this.addUncategorisedParameter(new UncategorisedParameter(parsedParam));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	/*
	 * Removes a DependencyParameter
	 */
	public void removeDependencyParameter(DependencyParameter dp) {
	    Iterator<DependencyParameter> iter = this.dependencyParameters.iterator();
	    while (iter.hasNext()) {
	        DependencyParameter current = iter.next();
	        if (current.getName().equals(dp.getName())) {
	            iter.remove(); // Safely remove from dependencyParameters
	            break; // Assuming names are unique, break out of the loop.
	        }
	    }
	}
	
	public void removeExternalParameter(ExternalParameter ep) {
	    Iterator<ExternalParameter> iter = this.externalParameters.iterator();
	    while (iter.hasNext()) {
	        ExternalParameter current = iter.next();
	        if (current.getName().equals(ep.getName())) {
	            iter.remove(); // Safely remove from dependencyParameters
	            break; // Assuming names are unique, break out of the loop.
	        }
	    }
	}
	
	/*
	 * Removes an UncategorisedParameter
	 */
	public void removeUncategorisedParameter(UncategorisedParameter uc) {
	    Iterator<UncategorisedParameter> iter = this.uncategorisedParameters.iterator();
	    while (iter.hasNext()) {
	        UncategorisedParameter current = iter.next();
	        if (current.getName().equals(uc.getName())) {
	            iter.remove(); // Safely remove from uncategorisedParameters
	            break; // Assuming names are unique, break out of the loop.
	        }
	    }
	}
	
	public String toString() {
		return this.modelId;
	}
}