package parameters;

import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Platform;
import learning.BayesianAverageCalculator;
import learning.MeanCalculator;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public abstract class ExternalParameter implements IStaticParameter {
	private String name;
	// private String value;
	// private Double rangedValue;
	// private ArrayList<String> rangedValues = new ArrayList<String>();

	public ExternalParameter(String name, String value) {
		this.name = name;
		// this.value = value;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public String getName() {
		return this.name;
	}

	// public void setValue(String value) throws Exception {
	// this.value = value;
	// };

	// public String getValue() throws IOException {
	// return value;
	// };

	public abstract String toString();

	public abstract void setValue(String value) throws Exception;

}
