package parameters;

import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Platform;
import learning.BayesianAverageCalculator;
import learning.MeanCalculator;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public class RangedExternalParameter extends ExternalParameter {
	private String name;
	private final String type = "ranged";
	private ArrayList<String> valueOptions = new ArrayList<String>();
	private String currentValue;

	public RangedExternalParameter(String name, ArrayList<String> valueOptions)
			throws NumberFormatException, IOException {
		super(name, null);
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
		return "External Parameter: " + name + "\nType: " + type + "\nValues: " +
				String.join(", ", valueOptions)
				+ "\n";
	}
}
