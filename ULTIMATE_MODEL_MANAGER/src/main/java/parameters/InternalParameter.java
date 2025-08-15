package parameters;

import jmetal.encodings.variable.Int;

public class InternalParameter implements IStaticParameter {
	private String name;
	private String type = "double";
	private String min;
	private String max;
	private Double interval;
	private String value;

	public InternalParameter(String name, String type, String min, String max, Double interval) {
		this.name = name;
		this.type = type;
		this.min = min;
		this.max = max;
		this.interval = interval;
	}

	public InternalParameter(String name, String min, String max, Double interval) {
		this.name = name;

		// if (("int".equals(type))) {
		// 	this.min = Integer.parseInt(min);
		// 	this.max = Integer.parseInt(max);
		// } else {
		// }
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

	public String getMin() {
		return this.min;
	}

	public String getMax() {
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

	public void setMin(String min) {
		this.min = min;
	}

	public void setMax(String max) {
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
