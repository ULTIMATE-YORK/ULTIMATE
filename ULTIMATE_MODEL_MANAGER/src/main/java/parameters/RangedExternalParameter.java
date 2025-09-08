package parameters;

import java.io.IOException;
import java.util.ArrayList;

public class RangedExternalParameter extends ExternalParameter {
	private final String type = "ranged";
	private ArrayList<String> valueOptions = new ArrayList<String>();
	private String currentValue;

	public RangedExternalParameter(String name, ArrayList<String> valueOptions, String uniqueIdentifier)
			throws NumberFormatException, IOException {
		super(name, uniqueIdentifier);
		this.valueOptions = valueOptions;

	}

	public ArrayList<String> getValueOptions() {
		return valueOptions;
	}

	public void setValue(String value) throws Exception {
		if (valueOptions.contains(value)) {
			this.currentValue = value;
		} else {
			throw new IOException(
					"Value '" + value + "' is invalid for RangedExternalParameter with options " + valueOptions + ".");
		}
	};

	@Override
	public String getValue() {
		return currentValue;
	}

	public String toString() {
		return "Ranged External Parameter: " + super.getNameInModel() + "\nType: " + type + "\nValues: " +
				String.join(", ", valueOptions)
				+ "\n";
	}

	public String getConfigCacheString() {
		return String.format("RangedExternalParameter:%s:%s", getNameInModel(), valueOptions);
	}
}
