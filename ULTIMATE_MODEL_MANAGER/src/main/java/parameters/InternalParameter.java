package parameters;

public class InternalParameter {
	private String name;
	private String minValue;
	private String maxValue;

	public InternalParameter(String name, String minValue, String maxValue) {
		this.name = name;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	// GETTER METHODS

	public String getName() {
		return this.name;
	}

	public String getMinValue() {
		return this.minValue;
	}

	public String getMaxValue() {
		return this.maxValue;
	}

	// SETTER METHODS

	public void setName(String newName) {
		this.name = newName;
	}

	public void setMinValue(String newMinValue) {
		this.minValue = newMinValue;
	}

	public void setMaxValue(String newMaxValue) {
		this.maxValue = newMaxValue;
	}

	public String toString() {
		return String.format("Internal Parameter: %s\nMinimum: %s\nMaximum: %s", name, minValue, maxValue); 
	}
}
