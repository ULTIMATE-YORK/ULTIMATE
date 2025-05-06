package parameters;

public class InternalParameter {
	private String name;
	private String type;
	private Double min;
	private Double max;
	
	public InternalParameter(String name, String type, Double min, Double max) {
		this.name = name;
		this.type = type;
		this.min = min;
		this.max = max;
	}
	
    // GETTER METHODS

    public String getName() {
    	return this.name;	
    }
    
	public String getType() {
		return this.type;
	}
	
	public Double getMin() {
		return this.min;
	}
	
	public Double getMax() {
		return this.max;
	}
    
    // SETTER METHODS
    
    public void setName(String newName) {
    	this.name = newName;
    }
    
    public void setType(String type) {
		this.type = type;
    }
    
	public void setRange(Double min, Double max) {
		this.min = min;
    }
    
    public String toString() {
    	return getName();
    }
}
