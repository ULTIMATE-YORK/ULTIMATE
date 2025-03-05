package parameters;

public class ExternalParameter {
    private String name;
    private String type;
    private String value;
    
    public ExternalParameter(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
    
    // GETTER METHODS
    
    public String getName() {
    	return this.name;	
    }
    
    public String getType() {
    	return this.type;
    }
    
    public String getValue() {
    	return this.value;
    }
    
    // SETTER METHODS
    
    public void setName(String newName) {
    	this.name = newName;
    }
    
    public void setType(String newtype) {
    	this.type = newtype;
    }
    
    public void setValue(String newValue) {
    	this.value = newValue;
    }
    public String toString() {
    	return "External Parameter: " + name + "\nType: " + type + "\nValue: " + value + "\n";
    }
}
