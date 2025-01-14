package persistent_objects;

public class EnvironmentParameter extends Parameter {
    private String name;
    private String filePath;
    private String calculation;
    
    public EnvironmentParameter(String name, String filePath, String calculation) {
        this.name = name;
        this.filePath = filePath;
        this.calculation = calculation;
    }
    
    // GETTER METHODS
    
    public String getName() {
    	return this.name;	
    }
    
    public String getFilePath() {
    	return this.filePath;
    }
    
    public String getCalculation() {
    	return this.calculation;
    }
    
    // SETTER METHODS
    
    public void setName(String newName) {
    	this.name = newName;
    }
    
    public void setFilePath(String newFilePath) {
    	this.filePath = newFilePath;
    }
    
    public void setCalculation(String newCalculation) {
    	this.calculation = newCalculation;
    }
    public String toString() {
    	return getName() + ", " + getFilePath() + ", " + getCalculation();
    }
}