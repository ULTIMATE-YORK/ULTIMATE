package parameters;

import model.Model;

public class DependencyParameter {
    private String name;
    private Model model;
    private String definition; // definition of the property to be verified on model
    // TODO make this a result type?
    private String result;
    
    public DependencyParameter(String name, Model model, String definition) {
        this.name = name;
        this.model = model;
        this.definition = definition;
    }
    
    // GETTER METHODS

    public String getName() {
    	return this.name;	
    }
    
    public Model getModel() {
    	return this.model;
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
    
    public void setModel(Model newModel) {
    	this.model = newModel;
    }
    
    public void setDefinition(String newDefinition) {
    	this.definition = newDefinition;
    }
    
	public void setResult(String newResult) {
		this.result = newResult;
	}
    
    public String toString() {
    	return "Dependency Parameter: " + name + "\nModel ID of dependency: " + model.getModelId() + " \nProperty definition to be verified on model" + definition + "\n";
    }
    
}
