package model.persistent_objects;

public class DependencyParameter extends Parameter {
    private String name;
    private String modelID;
    private String definition;
    private String result;
    
    public DependencyParameter(String name, String modelID, String definition) {
        this.name = name;
        this.modelID = modelID;
        this.definition = definition;
    }
    
    // GETTER METHODS

    public String getName() {
    	return this.name;	
    }
    
    public String getModelID() {
    	return this.modelID;
    }
    
    public String getDefinition() {
    	return this.definition;
    }
    
	public String getResult() {
		return this.result;
	}
    
    // SETTER METHODS
    
    public void setName(String newName) {
    	this.name = newName;
    }
    
    public void setModelID(String newModelID) {
    	this.modelID = newModelID;
    }
    
    public void setDefinition(String newDefinition) {
    	this.definition = newDefinition;
    }
    
	public void setResult(String newResult) {
		this.result = newResult;
	}
    
    public String toString() {
    	return getName() + ", " + getModelID() + ", " + getDefinition();
    }
}