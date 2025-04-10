package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import parameters.DependencyParameter;
import parameters.ExternalParameter;
import parameters.UncategorisedParameter;
import property.Property;
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
    private ObservableList<UncategorisedParameter> uncategorisedParameters; // List of undefined parameters
    private ObservableList<Property> properties;
    private File verificationFile;
    
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
        this.uncategorisedParameters = FXCollections.observableArrayList();
        this.properties = FXCollections.observableArrayList();
        this.verificationFile = tempModelFile();
    }
    
    /*
     * Adds a property to the model
     * 
     * @param newProp the property to add
     * @return true if the property was added, false otherwise
     */
    public boolean addProperty(String newProp) {
    	for (Property p : properties) {
    		if (p.getProperty().equals(newProp)) {	// check the property is novel
    			return false;
    		}
    	}
		properties.add(new Property(newProp));
		return true;
    }
    
    /*
     * Removes a property from the model
     * 
     * @param remove the property to remove
     * @return true if the property was removed, false otherwise
     */
    public boolean removeProperty(Property remove) {
    	return properties.remove(remove);
    }
    
    public ObservableList<Property> getProperties() {
    	return properties;
    }

    /**
     * Adds a dependency parameter to the model.
     * 
     * @param parameter the dependency parameter to add
     * @return true if the parameter was added, false otherwise
     */   
    public boolean addDependencyParameter(DependencyParameter parameter) {
    	for (DependencyParameter dp : dependencyParameters) {
    		if (dp.getName().equals(parameter.getName())) {
    			return false; // Parameter with the same name already exists
    		}
    	}
    	dependencyParameters.add(parameter);
    	return true; // Parameter added successfully
    }

	/*
	 * Removes a DependencyParameter from the list of dependency parameters
	 * 
	 * @param dp the dependency parameter to remove
	 * @return true if the parameter was removed, false otherwise
	 */
	public boolean removeDependencyParameter(DependencyParameter dp) {
	    Iterator<DependencyParameter> iter = this.dependencyParameters.iterator();
	    while (iter.hasNext()) {
	        DependencyParameter current = iter.next();
	        if (current.getName().equals(dp.getName())) {
	            iter.remove(); // Safely remove from dependencyParameters
	            return true; // Assuming names are unique, break out of the loop.
	        }
	    }
	    return false; // If not found, return false
	}
	
	/**
	 * Sets the list of dependency parameters for the model.
	 * 
	 * @param parameters the list of dependency parameters to add
	 */
	public void setDependencyParameters(ObservableList<DependencyParameter> parameters) {
		dependencyParameters = parameters;
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
     * Adds an environment parameter to the model.
     * 
     * @param parameter the external parameter to add
     * @return true if the parameter was added, false otherwise
     */
    public boolean addExternalParameter(ExternalParameter parameter) {
    	for (ExternalParameter ep : externalParameters) {
    		if (ep.getName().equals(parameter.getName())) {
    			return false; // Parameter with the same name already exists
    		}
    	}
    	externalParameters.add(parameter);
    	return true; // Parameter added successfully
    }

	/*
	 * Removes an ExternalParameter from the list of environment parameters
	 * 
	 * @param ep the environment parameter to remove
	 * @return true if the parameter was removed, false otherwise
	 */
	public boolean removeExternalParameter(ExternalParameter ep) {
	    Iterator<ExternalParameter> iter = this.externalParameters.iterator();
	    while (iter.hasNext()) {
	        ExternalParameter current = iter.next();
	        if (current.getName().equals(ep.getName())) {
	            iter.remove(); // Safely remove from dependencyParameters
	            return true; // Assuming names are unique, break out of the loop.
	        }
	    }
	    return false; // If not found, return false
	}
	
	/*
	 * Sets the list of environment parameters for the model.
	 * 
	 * @param parameters the list of environment parameters to add
	 */
	public void setExternalParameters(ObservableList<ExternalParameter> parameters) {
		externalParameters = parameters;
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
     * Adds an uncategorised parameter to the model.
     * 
     * @param parameter the uncategorised parameter to add
     * @return true if the parameter was added, false otherwise
     */
    public boolean addUncategorisedParameter(UncategorisedParameter parameter) {
    	for (UncategorisedParameter up : uncategorisedParameters) {
    		if (up.getName().equals(parameter.getName())) {
    			return false; // Parameter with the same name already exists
    		}
    	}
    	uncategorisedParameters.add(parameter);
    	return true; // Parameter added successfully
    }
	
	/*
	 * Removes an UncategorisedParameter from the list of uncategorised parameters
	 * 
	 * @param uc the uncategorised parameter to remove
	 * @return true if the parameter was removed, false otherwise
	 */
	public boolean removeUncategorisedParameter(UncategorisedParameter uc) {
	    Iterator<UncategorisedParameter> iter = this.uncategorisedParameters.iterator();
	    while (iter.hasNext()) {
	        UncategorisedParameter current = iter.next();
	        if (current.getName().equals(uc.getName())) {
	            iter.remove(); // Safely remove from uncategorisedParameters
	            return true; // Assuming names are unique, break out of the loop.
	        }
	    }
	    return false; // If not found, return false
	}
	
	/*
	 * Sets the list of uncategorised parameters for the model.
	 * 
	 * @param parameters the list of uncategorised parameters to add
	 */
	public void setUncategorisedParameters(ObservableList<UncategorisedParameter> parameters) {
		uncategorisedParameters = parameters;
	}
	
    /**
     * Gets the list of uncategorised parameters associated with the model.
     * 
     * @return the list of uncategorised parameters
     */
    public ObservableList<UncategorisedParameter> getUncategorisedParameters() {
        return uncategorisedParameters;
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
     * Gets the model's file path to the original prism model file.
     * 
     * @return the file path
     */
    public String getFilePath() {
        return this.filePath;
    }

    /**
     * Sets a new prism file path for the model.
     * 
     * @param filePath2 the new file path
     * @throws IOException if the file is not a valid PRISM model
     */
    public void setFilePath(String filePath) throws IOException {
    	if (FileUtils.isPrismFile(filePath)) {
    		this.filePath = filePath;
    	}
    }
    
    /*
     * Method to get the list of external parameters in a HashMap where name is key and value is the evaluated value of the parameter
     * 
     * @return a HashMap of external parameters
     */
    public HashMap<String, Double> getHashExternalParameters() throws NumberFormatException, IOException {
		HashMap<String, Double> hash = new HashMap<>();
		for (ExternalParameter ep : externalParameters) {
			hash.put(ep.getName(), ep.evaluate());
		}
		return hash;
    }
        
	/*
	 * Adds the uncategorised parameters to the model by parsing the prism file given by the file path
	 */
    public void addUncategorisedParametersFromFile() {
        PrismFileParser parser = new PrismFileParser();
        try {
            List<String> params = parser.parseFile(this.getFilePath());
            for (String parsedParam : params) {
                boolean dexists = dependencyParameters.stream()
                    .anyMatch(dp -> dp.getName().equals(parsedParam));
                boolean eexists = externalParameters.stream().anyMatch(ep -> ep.getName().equals(parsedParam));
                if (!dexists && !eexists) {
                    this.addUncategorisedParameter(new UncategorisedParameter(parsedParam));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	/*
	 * Method to create and return a copy of the model file to be used for verification
	 */
	private File tempModelFile() throws IOException {
	    // Create a temporary file. The prefix and suffix can be adjusted as needed.
	    File tempFile = File.createTempFile(this.modelId + "_copy", ".prism");
	    // Copy the original file (located at filePath) to the temporary file.
	    File originalFile = new File(this.filePath);
	    Files.copy(originalFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    // update the file with the model parameters;
	    FileUtils.writeParametersToFile(tempFile.getAbsolutePath(), getHashExternalParameters());
	    // Ensure that the temporary file is deleted when the JVM exits.
	    tempFile.deleteOnExit();
	    
	    return tempFile;
	}

	/*
	 * Method to get the path to the temporary model file
	 * 
	 * @return the path to the temporary model file
	 */
	public String getVerificationFilePath() throws IOException {
		//verificationFile = tempModelFile();
		return verificationFile.getAbsolutePath();
	}
	
	/*
	 * Override the toString method to return the model ID
	 * 
	 * @return the model ID
	 */
	@Override
	public String toString() {
		return this.modelId;
	}
}