package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import parameters.DependencyParameter;
import parameters.ExternalParameter;
import parameters.InternalParameter;
import parameters.UncategorisedParameter;
import property.Property;
import utils.Alerter;
import utils.FileUtils;
import utils.PrismFileParser;

/**
 * Represents a model in the system.
 * This class stores information about a model, including its unique ID, file
 * path,
 * and various types of parameters associated with the model (e.g., dependency
 * parameters, environment parameters).
 */
public class Model {
	private String modelId; // Unique identifier for the model
	private String filePath; // Path to the model's file
	// private String propertiesFile; // file of properties list
	private ObservableList<DependencyParameter> dependencyParameters; // List of dependency parameters
	private ObservableList<ExternalParameter> externalParameters; // List of environment parameters
	private ObservableList<InternalParameter> internalParameters; // List of internal parameters
	private ObservableList<UncategorisedParameter> uncategorisedParameters; // List of undefined parameters
	private ObservableList<Property> properties;
	private File verificationFile;
	private HashMap<String, HashMap<String, Double>> results = new HashMap<String, HashMap<String, Double>>(); // Results
																												// of
																												// the
																												// model
																												// verification

	/**
	 * Constructor to initialise a new Model object.
	 *
	 * @param filePath the file path of the model
	 * @throws IOException if the file is not a valid PRISM model
	 */
	public Model(String filePath) throws IOException {
		this.modelId = FileUtils.removePrismFileExtension(filePath); // will throw an error if the file is not prism
																		// file
		this.filePath = filePath;
		this.dependencyParameters = FXCollections.observableArrayList();
		this.externalParameters = FXCollections.observableArrayList();
		this.internalParameters = FXCollections.observableArrayList();
		this.uncategorisedParameters = FXCollections.observableArrayList();
		// addUncategorisedParametersFromFile();

		this.properties = FXCollections.observableArrayList();
		this.verificationFile = tempModelFile();
	}

	public void addProperty(String newProp) {
		// check the property is novel
		for (Property p : properties) {
			if (p.getProperty().equals(newProp)) {
				Alerter.showWarningAlert("Property Already Exists", "The property could not be added to the model");
				return;
			}
		}
		// Alerter.showInfoAlert("SUCCESS", "The property was added");
		properties.add(new Property(newProp));
	}

	public void removeProperty(Property remove) {
		boolean removed = properties.remove(remove);
		if (removed) {
			Alerter.showInfoAlert("SUCCESS", "The property was removed");
		} else {
			Alerter.showWarningAlert("FAILED", "The property could not be removed!");
		}
	}

	public ObservableList<Property> getProperties() {
		return properties;
	}

	/**
	 * Adds a dependency parameter to the model.
	 * 
	 * @param parameter the dependency parameter to add
	 */
	public void addDependencyParameter(DependencyParameter parameter) {
		dependencyParameters.add(parameter);
	}

	/*
	 * Adds a list of dependency parameters to the model.
	 * 
	 * @param parameters the list of dependency parameters to add
	 */
	public void setDependencyParameters(ObservableList<DependencyParameter> parameters) {
		dependencyParameters = parameters;
	}

	/**
	 * Adds an environment parameter to the model.
	 * 
	 * @param parameter the dependency parameter to add
	 */
	public void addExternalParameter(ExternalParameter parameter) {
		externalParameters.add(parameter);
	}

	/*
	 * Adds a list of environment parameters to the model.
	 * 
	 * @param parameters the list of environment parameters to add
	 */
	public void setExternalParameters(ObservableList<ExternalParameter> parameters) {
		externalParameters = parameters;
	}

	public ExternalParameter getExternalParameter(String name) {
		for (ExternalParameter ep : externalParameters) {
			if (ep.getName().equals(name)) {
				return ep;
			}
		}
		return null;
	}

	/**
	 * Adds an internal parameter to the model.
	 * 
	 * @param parameter the dependency parameter to add
	 */
	public void addInternalParameter(InternalParameter parameter) {
		internalParameters.add(parameter);
	}

	/*
	 * Adds a list of internal parameters to the model.
	 * 
	 * @param parameters the list of internal parameters to add
	 */
	public void setInternalParameters(ObservableList<InternalParameter> parameters) {
		internalParameters = parameters;
	}

	/**
	 * Adds an uncategorised parameter to the model.
	 * 
	 * @param parameter the uncategorised parameter to add
	 */
	public void addUncategorisedParameter(UncategorisedParameter parameter) {
		uncategorisedParameters.add(parameter);
	}

	/*
	 * Adds a list of uncategorised parameters to the model.
	 * 
	 * @param parameters the list of uncategorised parameters to add
	 */
	public void setUncategorisedParameters(ObservableList<UncategorisedParameter> parameters) {
		uncategorisedParameters = parameters;
	}

	/**
	 * Gets the model's unique identifier.
	 * 
	 * @return the model ID
	 */
	public String getModelId() {
		return this.modelId;
	}

	/**
	 * Gets the model's file path.
	 * 
	 * @return the file path
	 */
	public String getFilePath() {
		return this.filePath;
	}

	/**
	 * Sets the model's file path.
	 * 
	 * @param filePath2 the new file path
	 * @throws IOException
	 */
	public void setFilePath(String filePath) throws IOException {
		if (FileUtils.isPrismFile(filePath)) {
			this.filePath = filePath;
		}
	}

	/**
	 * Gets the list of dependency parameters associated with the model.
	 * 
	 * @return the list of dependency parameters
	 */
	public ObservableList<DependencyParameter> getDependencyParameters() {
		return dependencyParameters;
	}

	/**
	 * Gets the list of environment parameters associated with the model.
	 * 
	 * @return the list of environment parameters
	 */
	public ObservableList<ExternalParameter> getExternalParameters() {
		return externalParameters;
	}

	public HashMap<String, Double> getHashExternalParameters() throws NumberFormatException, IOException {
		HashMap<String, Double> hash = new HashMap<>();
		for (ExternalParameter ep : externalParameters) {
			hash.put(ep.getName(), ep.evaluate());
		}
		return hash;
	}

	public HashMap<String, InternalParameter> getHashInternalParameters() throws NumberFormatException, IOException {
		HashMap<String, InternalParameter> hash = new HashMap<>();
		for (InternalParameter ip : internalParameters) {
			hash.put(ip.getName(), ip);
		}
		return hash;
	}

	/**
	 * Gets the list of internal parameters associated with the model.
	 * 
	 * @return the list of internal parameters
	 */
	public ObservableList<InternalParameter> getInternalParameters() {
		return internalParameters;
	}

	/**
	 * Gets the list of uncategorised parameters associated with the model.
	 * 
	 * @return the list of uncategorised parameters
	 */
	public ObservableList<UncategorisedParameter> getUncategorisedParameters() {
		return uncategorisedParameters;
	}

	/*
	 * Adds the uncategorised parameters to the model
	 */
	public void addUncategorisedParametersFromFile() {
		PrismFileParser parser = new PrismFileParser();
		try {
			List<String> params = parser.parseFile(this.getFilePath());
			for (String parsedParam : params) {
				boolean dexists = dependencyParameters.stream()
						.anyMatch(dp -> dp.getName().equals(parsedParam));
				boolean eexists = externalParameters.stream().anyMatch(ep -> ep.getName().equals(parsedParam));
				if (!dexists && !eexists) {
					this.addUncategorisedParameter(new UncategorisedParameter(parsedParam));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Removes a DependencyParameter
	 */
	public void removeDependencyParameter(DependencyParameter dp) {
		Iterator<DependencyParameter> iter = this.dependencyParameters.iterator();
		while (iter.hasNext()) {
			DependencyParameter current = iter.next();
			if (current.getName().equals(dp.getName())) {
				iter.remove(); // Safely remove from dependencyParameters
				break; // Assuming names are unique, break out of the loop.
			}
		}
	}

	public void removeExternalParameter(ExternalParameter ep) {
		Iterator<ExternalParameter> iter = this.externalParameters.iterator();
		while (iter.hasNext()) {
			ExternalParameter current = iter.next();
			if (current.getName().equals(ep.getName())) {
				iter.remove(); // Safely remove from dependencyParameters
				break; // Assuming names are unique, break out of the loop.
			}
		}
	}

	/*
	 * Removes an UncategorisedParameter
	 */
	public void removeUncategorisedParameter(UncategorisedParameter uc) {
		Iterator<UncategorisedParameter> iter = this.uncategorisedParameters.iterator();
		while (iter.hasNext()) {
			UncategorisedParameter current = iter.next();
			if (current.getName().equals(uc.getName())) {
				iter.remove(); // Safely remove from uncategorisedParameters
				break; // Assuming names are unique, break out of the loop.
			}
		}
	}

	public void removeInternalParameter(InternalParameter ip) {
		Iterator<InternalParameter> iter = this.internalParameters.iterator();
		while (iter.hasNext()) {
			InternalParameter current = iter.next();
			if (current.getName().equals(ip.getName())) {
				iter.remove(); // Safely remove from internalParameters
				break; // Assuming names are unique, break out of the loop.
			}
		}
	}

	public ArrayList<HashMap<String, Double>> getCartesianExternal() {
		ArrayList<ExternalParameter> rangedEPs = new ArrayList<>();
		for (ExternalParameter ep : externalParameters) {
			if (ep.getType().equals("Ranged")) {
				rangedEPs.add(ep);
			}
		}
		ArrayList<HashMap<String, Double>> results = new ArrayList<>();
		backtrack(rangedEPs, 0, new HashMap<>(), results);
		return results;
	}

	private void backtrack(ArrayList<ExternalParameter> params, int index, HashMap<String, Double> current,
			ArrayList<HashMap<String, Double>> results) {
		if (index == params.size()) {
			results.add(new HashMap<>(current));
			return;
		}

		ExternalParameter param = params.get(index);
		String name = param.getName();
		ArrayList<Double> values = param.getRangedValues();

		for (double val : values) {
			current.put(name, val);
			backtrack(params, index + 1, current, results);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Model model = (Model) o;
		return Objects.equals(modelId, model.modelId); // assuming modelId is a unique identifier
	}

	@Override
	public int hashCode() {
		return Objects.hash(modelId);
	}

	public boolean isRangedModel() {
		for (ExternalParameter ep : this.externalParameters) {
			if (ep.getType().equals("Ranged")) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Method to create and return a copy of the model file to be used for
	 * verification
	 */
	private File tempModelFile() throws IOException {
		// Create a temporary file. The prefix and suffix can be adjusted as needed.
		File tempFile = File.createTempFile(this.modelId + "_copy", ".prism");
		// Copy the original file (located at filePath) to the temporary file.
		File originalFile = new File(this.filePath);
		Files.copy(originalFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		// update the file with the model parameters;
		FileUtils.writeParametersToFile(tempFile.getAbsolutePath(), getExternalParameters(),
				getInternalParameters());
		// Ensure that the temporary file is deleted when the JVM exits.
		// tempFile.deleteOnExit();

		return tempFile;
	}

	public String getVerificationFilePath() throws IOException {
		// verificationFile = tempModelFile();
		return verificationFile.getAbsolutePath();
	}

	public ArrayList<String> rangedToString() {
		ArrayList<String> ret = new ArrayList<String>();
		for (HashMap<String, Double> configs : this.getCartesianExternal()) {
			String basic = this.toString();
			for (String key : configs.keySet()) {
				basic += "\n" + key + " : " + configs.get(key) + "\n";
			}
			ret.add(basic);
		}
		return ret;
	}

	public String toString() {
		return this.modelId + internalParameters.toString() + externalParameters.toString()
				+ dependencyParameters.toString() + uncategorisedParameters.toString();
	}

	public void addResult(String prop, HashMap<String, Double> configResult) {
		if (results.containsKey(prop)) {
			results.get(prop).putAll(configResult);
		} else {
			results.put(prop, configResult);
		}
	}

	public Double getResult(String prop, String config) {
		if (results.containsKey(prop)) {
			return results.get(prop).get(config);
		} else {
			return null;
		}
	}

	public HashMap<String, Double> getResultMap(String prop) {
		return results.get(prop);
	}

	public void setInternalParametersFromHashMap(HashMap<String, String> hashInternalParameters) {

		for (InternalParameter ip : internalParameters) {
			for (String key : hashInternalParameters.keySet()) {
				// System.out.println(key + "  " + hashInternalParameters.get(key));
				if (ip.getName().equals(key)) {
					ip.setValue(hashInternalParameters.get(key));
					break;
				}
			}
		}

	}

	public void setInternalParameterValue(String name, String value) {
		for (InternalParameter ip : internalParameters) {
			if (ip.getName().equals(name)) {
				ip.setValue(value);
				return;
			}
		}
		Alerter.showWarningAlert("Parameter Not Found",
				"The internal parameter with name '" + name + "' was not found in the model.");
	}
}