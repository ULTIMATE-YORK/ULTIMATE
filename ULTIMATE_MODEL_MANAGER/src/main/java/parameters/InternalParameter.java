package parameters;

import jmetal.encodings.variable.Int;

public class InternalParameter {
	private String name;
	private String type = "double";
	private Number min;
	private Number max;
	private Double interval;
	private String value;

	public InternalParameter(String name, String type, Number min, Number max, Double interval) {
		this.name = name;
		this.type = type;
		this.min = min;
		this.max = max;
		this.interval = interval;
	}

	public InternalParameter(String name, String type, String min, String max, Double interval) {
		this.name = name;
		this.type = type;

		if (("int".equals(type))) {
			this.min = Integer.parseInt(min);
			this.max = Integer.parseInt(max);
		} else {
			this.min = Double.parseDouble(min);
			this.max = Double.parseDouble(max);
		}

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

	public Number getMin() {
		return this.min;
	}

	public Number getMax() {
		return this.max;
	}

	public Double getInterval() {
		return this.interval;
	}

	public String getValue() {
		return this.value;
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

	public void setValue(String value) {
		// if(type != "boolean"){
		// double parsedValue = Double.parseDouble(value);
		// if (parsedValue < min || parsedValue > max) {
		// throw new IllegalArgumentException("Value out of bounds: " + value + ". Must
		// be between " + min + " and " + max);
		// }
		// }
		this.value = value;
	}

	public String toString() {
		return "Internal Parameter: " + getName() + "\nType: " + getType() + "\nMin: " + getMin() + "\nMax: " + getMax()
				+ "\nInterval: " + getInterval() + "\n";
	}
}
