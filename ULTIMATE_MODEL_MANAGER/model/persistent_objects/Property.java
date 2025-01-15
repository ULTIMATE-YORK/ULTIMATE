package persistent_objects;
// TODO Implement the Property class with validation on property strings

public class Property {
	
	private String modelID; 
	private String definition;
	
	public Property(String modelID, String definition) {
		isDefValid(definition);
		this.modelID = modelID;
		this.definition = definition;
	}
	
	private void isDefValid(String definition) {
		// TODO validate the string, throw exception if not valid
	}
	
	public void setDefinition(String newDefinition) {
		this.definition = newDefinition;
	}
	
	public String getDefinition() {
		return this.definition;
	}
	
	public void setModelID(String newModelID) {
		this.modelID = newModelID;
	}
	
	public String getModelID() {
		return this.modelID;
	}
}
