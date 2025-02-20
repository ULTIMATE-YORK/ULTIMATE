package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import parameters.DependencyParameter;
import parameters.EnvironmentParameter;
import parameters.InternalParameter;
import parameters.UncategorisedParameter;
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
    private List<DependencyParameter> dependencyParameters; // List of dependency parameters
    private List<EnvironmentParameter> environmentParameters; // List of environment parameters
    private List<InternalParameter> internalParameters; // List of internal parameters
    private List<UncategorisedParameter> uncategorisedParameters; // List of undefined parameters
    //private ArrayList<Property> properties;
    
    /**
     * Constructor to initialise a new Model object.
     *
     * @param filePath the file path of the model
     * @throws IOException if the file is not a valid PRISM model
     */
    public Model(String filePath) throws IOException {
        this.modelId = FileUtils.removePrismFileExtension(filePath); // will throw an error if the file is not prism file
        this.filePath = filePath;
        this.dependencyParameters = new ArrayList<>();
        this.environmentParameters = new ArrayList<>();
        this.internalParameters = new ArrayList<>();
        this.uncategorisedParameters = new ArrayList<>();
        addUncategorisedParametersFromFile();
        
        //this.properties = new ArrayList<>();
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
	public void setDependencyParameters(List<DependencyParameter> parameters) {
		dependencyParameters = parameters;
	}
	
    /**
     * Adds an environment parameter to the model.
     * 
     * @param parameter the dependency parameter to add
     */
	public void addEnvironmentParameter(EnvironmentParameter parameter) {
		environmentParameters.add(parameter);
	}
	
	/*
	 * Adds a list of environment parameters to the model.
	 * 
	 * @param parameters the list of environment parameters to add
	 */
	public void setEnvironmentParameters(List<EnvironmentParameter> parameters) {
		environmentParameters = parameters;
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
	public void setUncategorisedParameters(List<UncategorisedParameter> parameters) {
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
    public List<DependencyParameter> getDependencyParameters() {
        return dependencyParameters;
    }
    
    /**
     * Gets the list of environment parameters associated with the model.
     * 
     * @return the list of environment parameters
     */
    public List<EnvironmentParameter> getEnvironmentParameters() {
        return environmentParameters;
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
    public List<UncategorisedParameter> getUncategorisedParameters() {
        return uncategorisedParameters;
    }
    
	/*
	 * Adds the uncategorised parameters to the model
	 */
	public void addUncategorisedParametersFromFile() {
        PrismFileParser parser = new PrismFileParser();
        try {
            List<String> params =  parser.parseFile(this.getFilePath());
            for (String parsedParam : params) {
            	this.addUncategorisedParameter(new UncategorisedParameter(parsedParam));
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}