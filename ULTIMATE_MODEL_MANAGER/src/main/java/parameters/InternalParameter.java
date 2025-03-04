package parameters;

public class InternalParameter {
	private String name;
	
	public InternalParameter(String name) {
		this.name = name;
	}
	
    // GETTER METHODS

    public String getName() {
    	return this.name;	
    }
    
    // SETTER METHODS
    
    public void setName(String newName) {
    	this.name = newName;
    }
    public String toString() {
    	return getName();
    }
}
