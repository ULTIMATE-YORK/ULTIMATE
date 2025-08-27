package property;

// TODO: add verification to strings
public class Property {
	
	private String definition;
	
	public Property(String property) {
		this.definition = property;
	}
	
	public void setDefinition(String newProp) {
		this.definition = newProp;
	}
	
	public String getDefinition() {
		return this.definition;
	}
	
	public String toString() {
		return getDefinition();
	}

}
