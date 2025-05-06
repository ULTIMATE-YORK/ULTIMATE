package parameters;

public class InternalParameter {
	private String name;
	private String type;
	private Double min;
	private Double max;
	private Double interval;
	
	public InternalParameter(String name, String type, Double min, Double max, Double interval) {
		this.name = name;
		this.type = type;
		this.min = min;
		this.max = max;
		this.interval = interval;
	}
	
	public InternalParameter(String name) {
		this.name = name;
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
	
	public Double getInterval() {
		return this.interval;
	}
    
    // SETTER METHODS
    
    public void setName(String newName) {
    	this.name = newName;
    }
    
    public void setType(String type) {
		this.type = type;
    }
	
	public void setInterval(Double interval) {
		this.interval = interval;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public void setMax(Double max) {
		this.max = max;
	}
    
    public String toString() {
    	return "Internal Parameter: " + getName() + "\nType: " + getType() + "\nMin: " + getMin() + "\nMax: " + getMax() + "\nInterval: " + getInterval() + "\n";
    }
}
