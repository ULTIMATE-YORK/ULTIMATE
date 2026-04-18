package parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import learning.BayesianAverageCalculator;
import learning.MeanCalculator;
import sharedContext.SharedContext;

public class LearnedExternalParameter extends ExternalParameter {
	private static final Logger logger = LoggerFactory.getLogger(LearnedExternalParameter.class);
	private String type;
	private String valueSource;
	private String value;
	public static final ArrayList<String> LEARNED_PARAMETER_TYPE_OPTIONS = new ArrayList<String>(
			List.of("mean", "mean-rate", "bayes", "bayes-rate"));

	public LearnedExternalParameter(String name, String type, String valueSource, String uniqueIdentifier)
			throws NumberFormatException, IOException {
		super(name, uniqueIdentifier);
		this.type = type.trim().toLowerCase();
		this.valueSource = valueSource;
	}

	@Override
	public String getValue() throws IOException {
		if (this.value == null) {
			this.value = evaluateParameterValue();
		}
		return this.value;
	}

	public void setValueSource(String source) {
		this.valueSource = source;
	}

	public String getValueSource() {
		return this.valueSource;
	}

	@Override
	public void setValue(String value) throws Exception {
		throw new Exception(String.format(
				"Cannot directly set a value on an external parameter that is of type 'learned' --- use 'setValueSource' instead.\nName: %s\nAttempted to set to value: %s",
				super.getNameInModel(), value));
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	// FIXME : os-dependent paths
	public String evaluateParameterValue() throws NumberFormatException, IOException {
		String result;
		switch (type) {
			case "mean":
				// FIXME : os-dependant paths
				result = MeanCalculator
						.computeMean(SharedContext.getProject().getDirectoryPath() + "/data/" + valueSource, ",")
						.toString();
				break;
			case "mean-rate":
				// FIXME : os-dependant paths
				result = MeanCalculator.computeMeanRate(
						SharedContext.getProject().getDirectoryPath() + "/data/" + valueSource, ",").toString();
				break;
			case "bayes":
				result = BayesianAverageCalculator.computeBayesianAverage(
						SharedContext.getProject().getDirectoryPath() + "/data/" + valueSource).toString();
				break;
			case "bayes-rate":
				result = BayesianAverageCalculator.computeBayesianAverageRate(
						SharedContext.getProject().getDirectoryPath() + "/data/" + valueSource).toString();
				break;
			// case "Ranged":
			// return rangedValue;
			default:
				throw new IOException("Could not evaluate the value of LearnedExternalParameter '" + super.getNameInModel()
						+ "'' with unknown type '" + type + "'");
		}
		logger.info("Computed external parameter '{}' in model '{}' (type: {}, source: {}) = {}", super.getNameInModel(), super.getUniqueIdentifier(), type, valueSource, result);
		return result;
	}

	public String toString() {
		try {
			return "Learned External Parameter: " + super.getNameInModel() + "\nType: " + type + "\nValue: "
					+ this.getValue()
					+ "\nSource: " + valueSource + "\n";
		} catch (IOException e) {
			return "Learned External Parameter: " + super.getNameInModel() + "\nType: " + type
					+ "\nValue: (calculation error)"
					+ "\nSource: " + valueSource + "\n";
		}

	}

	public String getConfigCacheString() {
		return String.format("LearnedExternalParameter:%s:%s:%s", getNameInModel(), getType(), getValueSource());
	}
}
