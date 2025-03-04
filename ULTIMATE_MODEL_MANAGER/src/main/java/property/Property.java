package property;

// TODO: add verification to strings
public class Property {
	
	private String property;
	
	public Property(String property) {
		this.property = property;
	}
	
	public void setProperty(String newProp) {
		this.property = newProp;
	}
	
	public String getProperty() {
		return this.property;
	}
	
	public String toString() {
		return getProperty();
	}

}
