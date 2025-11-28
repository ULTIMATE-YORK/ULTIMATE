package project;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.stream.Collectors;
import utils.ParameterUtilities;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import model.Model;
import parameters.DependencyParameter;
import parameters.ExternalParameter;
import parameters.IParameter;
import parameters.IStaticParameter;
import parameters.InternalParameter;
import parameters.RangedExternalParameter;
import parameters.SynthesisGoal;
import property.Property;
import sharedContext.SharedContext;
import utils.Alerter;
//import utils.Alerter;
import utils.FileUtils;

public class Project {

	private static final Logger logger = LoggerFactory.getLogger(Project.class);
	private boolean isModified = false; // Track if project has unsaved changes

	private Set<Model> models; // set of models in the project
	private String projectPath;
	private ObservableList<Model> observableModels; // The observable list used for UI binding
	private ObjectProperty<Model> targetModel; // Observable property of current model
	private String projectName;
	private ProjectImporter importer;
	private ProjectExporter exporter;
	private String stormInstall = null;
	private String stormParsInstall = null;
	private String prismInstall = null;
	private String prismGamesInstall = null;
	private String pythonInstall = null;
	private ObjectProperty<String> chosenPMC;
	private String saveLocation = null; // set when a project has been saved as, used for subsequent saves
	private String directory = null;
	private boolean configured = true;
	// private SharedContext SharedContext = SharedContext.getContext();
	private boolean isBlank;
	// key will be model id + property and second hashmap will be mapping of
	// configuration to result
	private HashMap<String, HashMap<String, String>> cache = new HashMap<String, HashMap<String, String>>(); // cache
																												// for
																												// storing
																												// results
																												// of
																												// generated
																												// models

	public Project(String projectPath) throws IOException {
		this.directory = Paths.get(projectPath).toAbsolutePath().getParent().toString();
		this.projectPath = projectPath;
		// SharedContext.setUltimateProject(this);
		FileUtils.isUltimateFile(projectPath); // throws IOE if file is not an ultimate project file
		importer = new ProjectImporter(projectPath);
		saveLocation = projectPath;
		models = importer.importProjectModels();
		for (Model model : models){
			model.setParentProject(this);
		}
		// Initialise currentModel property (first model in the set or null)
		targetModel = new SimpleObjectProperty<>(models.isEmpty() ? null : models.iterator().next());
		// Initialise the observable list with the contents of the set
		observableModels = FXCollections.observableArrayList(models);
		this.projectName = FileUtils.removeUltimateFileExtension(projectPath);
		if (SharedContext.getMainStage() != null) {
			SharedContext.getMainStage().setTitle("Ultimate Multi-Model Verifier: " + projectName);
		}
		try {
			setupConfigs();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		chosenPMC = new SimpleObjectProperty<>(
				prismInstall != null ? "PRISM" : (stormInstall != null ? "STORM" : null));
		exporter = new ProjectExporter(this);
		this.isBlank = false;
	}

	public Project() {
		this.models = new HashSet<Model>();
		this.observableModels = FXCollections.observableArrayList();
		this.targetModel = new SimpleObjectProperty<>(null);
		this.projectName = "Untitled";
		// SharedContext.setProject(this);
		if (SharedContext.getMainStage() != null) {
			SharedContext.getMainStage().setTitle("Ultimate Multi-Model Verifier: " + projectName);
		}
		try {
			setupConfigs();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		chosenPMC = new SimpleObjectProperty<>(
				prismInstall != null ? "PRISM" : (stormInstall != null ? "STORM" : null));
		exporter = new ProjectExporter(this);
		this.isBlank = true;
	}

	public ArrayList<String> getModelIDs() {
		ArrayList<String> modelIDs = new ArrayList<String>();
		models.forEach(model -> {
			modelIDs.add(model.getModelId());
		});
		return modelIDs;
	}

	public ObservableList<Model> getObservableModels() {
		return observableModels;
	}

	public void addModel(Model addModel) {
		// Check if the model already exists using the set
		for (Model model : models) {
			if (model.getModelId().equals(addModel.getModelId())) {
				throw new IllegalArgumentException();
				// Alerter.showErrorAlert("Model Already Exists!", "The model could not be added
				// as it exists in the project.");
			}
		}
		// Add to the set
		models.add(addModel);
		// Update the observable list
		observableModels.add(addModel);
		if (this.isBlank) {
			this.isBlank = false;
		}
		addModel.setParentProject(this);
		markAsModified();
	}

	public void removeModel(Model removeModel) {
		// Remove from the set (by id comparison)
		models.removeIf(model -> model.getModelId().equals(removeModel.getModelId()));
		// Remove from the observable list
		observableModels.removeIf(model -> model.getModelId().equals(removeModel.getModelId()));
		markAsModified();
	}

	public String getProjectName() {
		return projectName;
	}

	public Set<Model> getModels() {
		return this.models;
	}

	// Getter for currentModel property (for binding)
	public ObjectProperty<Model> currentModelProperty() {
		return targetModel;
	}

	public void setCurrentModel(Model model) {
		this.targetModel.set(model);
	}

	public Model getTargetModel() {
		return targetModel.get();
	}

	public ObjectProperty<String> chosenPMCProperty() {
		return chosenPMC;
	}

	public void setChosenPMC(String chosenPMC) {
		this.chosenPMC.set(chosenPMC);
	}

	public String getChosenPMC() {
		return chosenPMC.get();
	}

	public void save(String saveLocation) {
		exporter.saveExport(saveLocation);
		// Update project name when saving to a new location
		try{
			this.projectName = FileUtils.removeUltimateFileExtension(saveLocation);
		}catch (IOException e){
			// If filename parsing fails, use the full path
			this.projectName = new File(saveLocation).getName();
		}
		markAsClean(); 
	}

	public void save() {
		exporter.saveExport(saveLocation);
		markAsClean(); 
	}

	public void setSaveLocation(String location) {
		this.saveLocation = location;
	}

	public void load() {
		try {
			models = importer.importProjectModels();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isBlank() {
		return this.isBlank;
	}

	public void refresh() {
		// Save the current model's ID (if any)
		Model oldModel = getTargetModel();
		String oldModelId = oldModel != null ? oldModel.getModelId() : null;

		save();
		load();

		// Update the currentModel property by finding the model in the reloaded list
		if (oldModelId != null) {
			// Search for a model with the same id in the new models set
			for (Model m : models) {
				if (m.getModelId().equals(oldModelId)) {
					setCurrentModel(m);
					return; // Found and set, so we can exit the method
				}
			}
		}
	}

	public String getSaveLocation() {
		return this.saveLocation;
	}

	public void setStormInstall(String stormInstall) {
		if (FileUtils.isFile(stormInstall)) {
			this.stormInstall = stormInstall;
		} else {
			Alerter.showWarningAlert("No Storm Installation found!",
					"Please configure the location of the storm install on your system!");
		}
	}

	public void setStormParsInstall(String stormParsInstall) {
		if (FileUtils.isFile(stormParsInstall)) {
			this.stormParsInstall = stormParsInstall;
		} else {
			Alerter.showWarningAlert("No Storm-Pars Installation found!",
					"Please configure the location of the storm-pars install on your system!");
		}
	}

	public String getStormInstall() {
		return this.stormInstall;
	}

	public String getStormParsInstall() {
		return this.stormParsInstall;
	}

	public String getPrismInstall() {
		return this.prismInstall;
	}

	public String getPrismGamesInstall() {
		return this.prismGamesInstall;
	}

	public String getPythonInstall() {
		return this.pythonInstall;
	}

	private void setupConfigs() throws IOException {

		ArrayList<String> errorMessages = new ArrayList<>();
		File configFile = null;
		String content = null;
		JSONObject configJSON = null;

		String ULTIMATE_DIR = System.getenv("ULTIMATE_DIR");
		if (ULTIMATE_DIR == null || ULTIMATE_DIR.equals("")) {
			errorMessages.add(String.format("ULTIMATE_DIR environment variable not set. Make sure it points to ultimate/ULTIMATE_MODEL_MANAGER"));
			configured = false;
		}
		try {
			configFile = new File(System.getenv("ULTIMATE_DIR") + "/config.json");
			content = new String(Files.readAllBytes(Paths.get(configFile.toURI())));
			configJSON = new JSONObject(content);
		} catch (IOException e) {
			errorMessages.add(String.format(
					"config.json could not be found. Make sure the ULTIMATE_DIR environment variable points to ultimate/ULTIMATE_MODEL_MANAGER and that the file is present with correct formatting."));
			configured = false;
		} catch (JSONException e) {

			errorMessages.add(String.format(
					"Configuration file was found but could not be parsed. Make sure it is in correct JSON format."));
			configured = false;
		}

		if (configFile != null && content != null && configJSON != null) {
			String stormInstall = configJSON.getString("stormInstall");
			if (FileUtils.isFile(stormInstall) && !stormInstall.equals("")) {
				this.stormInstall = stormInstall;
			} else {
				errorMessages.add(String.format("Invalid Storm path in config.json: \"%s\" ", stormInstall));
				configured = false;

			}

			String stormParsInstall = configJSON.getString("stormParsInstall");
			if (FileUtils.isFile(stormParsInstall) && !stormParsInstall.equals("")) {
				this.stormParsInstall = stormParsInstall;
			} else {
				errorMessages.add(String.format("Invalid Storm-Pars path in config.json: \"%s\" ", stormParsInstall));
				configured = false;

			}

			String prismInstall = configJSON.getString("prismInstall");
			if (FileUtils.isFile(prismInstall) && !prismInstall.equals("")) {
				this.prismInstall = prismInstall;
			} else {
				errorMessages.add(String.format("Invalid PRISM path in config.json: \"%s\" ", prismInstall));
				configured = false;
			}

			String prismGamesInstall = configJSON.getString("prismGamesInstall");
			if (FileUtils.isFile(prismGamesInstall) && !prismGamesInstall.equals("")) {
				this.prismGamesInstall = prismGamesInstall;
			} else {
				errorMessages.add(String.format("Invalid PRISM-games path in config.json: \"%s\" ", prismGamesInstall));
				configured = false;
			}

			String pythonInstall = configJSON.getString("pythonInstall");
			if (FileUtils.isFile(pythonInstall) && !pythonInstall.equals("")) {
				this.pythonInstall = pythonInstall;
			} else {
				errorMessages.add(String.format("Invalid Python path in config.json: \"%s\" ", pythonInstall));
				configured = false;
			}
		}

		if (SharedContext.getMainStage() != null && !configured) {
			Alerter.showWarningAlert("Incorrect Configuration",
					String.format("The following issues were detected with your configuration:\n\n - %s"
							+ "\n\nPlease correct these issues and relaunch ULTIMATE.",
							String.join("\n\n - ", errorMessages)));
		}
	}

	// // TODO Generate a temporary directory of evolable model files for evochecker
	// public void generateEvoModels() {

	// }

	public boolean containsRangedParameters() {
		for (Model model : models) {
			for (ExternalParameter parameter : model.getExternalParameters()) {
				if (parameter instanceof RangedExternalParameter) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsInternalParameters() {
		for (Model model : models) {
			if (model.getInternalParameters().size() > 0) {
				return true;
			}
		}
		return false;
	}

	// TODO : should probably be renamed because of the UUID
	public HashMap<String, ArrayList<String>> getRangedExternalVariableValueOptions() {
		HashMap<String, ArrayList<String>> experimentConfiguration = new HashMap<>();

		for (Model m : models) {
			for (ExternalParameter ep : m.getExternalParameters()) {
				if (ep instanceof RangedExternalParameter) {
					experimentConfiguration.put(
							ParameterUtilities.generateUniqueParameterId(m.getModelId(), ep.getNameInModel()),
							((RangedExternalParameter) ep).getValueOptions());
				}
			}
		}

		return experimentConfiguration;
	}

	// public HashMap<String, HashMap<String, ArrayList<String>>>
	// getRangedExternalVariableValues() {
	// HashMap<String, HashMap<String, ArrayList<String>>> experimentConfiguration =
	// new HashMap<>();

	// for (Model m : models) {
	// HashMap<String, ArrayList<String>> thisModelHashMap = new HashMap<>();
	// for (ExternalParameter ep : m.getExternalParameters()) {
	// if (ep instanceof RangedExternalParameter) {
	// thisModelHashMap.put(ep.getNameInModel(), ((RangedExternalParameter)
	// ep).getValueOptions());
	// }
	// }
	// experimentConfiguration.put(m.getModelId(), thisModelHashMap);
	// }

	// return experimentConfiguration;
	// }

	public ArrayList<HashMap<String, String>> generateExperimentPlan() {

		HashMap<String, ArrayList<String>> rangedExternalVariableValues = getRangedExternalVariableValueOptions();
		ArrayList<HashMap<String, String>> experimentPlan = getAllCombinations(new ArrayList<HashMap<String, String>>(),
				rangedExternalVariableValues, 0);

		return experimentPlan;

	}

	private ArrayList<HashMap<String, String>> getAllCombinations(ArrayList<HashMap<String, String>> combinations,
			HashMap<String, ArrayList<String>> experimentConfig, int depth) {

		/*
		 * How this works:
		 * 
		 * One experimental iteration is defined by a hashmap with property names as
		 * keys and property values and values
		 * We want to construct all possible combinations of property values (i.e. the
		 * cartesian product of the property value sets)
		 * 
		 * Step 1: Recur down to the last property in our list of properties (n). We
		 * have a complete list of possible combinations as if property n was the only
		 * property. We return this.
		 * 
		 * Step 2: On the level above (n-1) we iterate through the possible values of
		 * n-1.
		 * We look at the 'combinations' from level n. To each combination, we add a
		 * unique
		 * value of n-1. We have a complete list of possible combinations as if
		 * properties
		 * n and n-1 were the only properties.
		 * 
		 * Step 3: On n-2, we iterate through the possible values of n-2. We look all
		 * the
		 * combinations of n and n-1. To each, we add a unique value of n-2. We have a
		 * complete list of all possible combinations as if properties n, n-1, n-2 were
		 * the only properties.
		 * 
		 * Step 4: Keep returning upwards until we have a complete set of combinations.
		 * 
		 */

		ArrayList<String> propertyNames = new ArrayList<>(experimentConfig.keySet());
		String propertyName = propertyNames.get(depth);

		ArrayList<String> thisDepthPropertyValues = experimentConfig.get(propertyName);
		ArrayList<HashMap<String, String>> newCombinations = new ArrayList<>();

		if (depth == propertyNames.size() - 1) {
			for (String value : thisDepthPropertyValues) {
				HashMap<String, String> bottomCombination = new HashMap<>();
				bottomCombination.put(propertyName, value);
				newCombinations.add(bottomCombination);
			}
		} else {
			ArrayList<HashMap<String, String>> allLowerCombinations = getAllCombinations(combinations, experimentConfig,
					depth + 1);
			for (String value : thisDepthPropertyValues) {
				for (HashMap<String, String> lc : allLowerCombinations) {
					HashMap<String, String> combination = new HashMap<>();
					combination.put(propertyName, value);
					combination.putAll(lc);
					newCombinations.add(combination);
				}
			}
		}

		return newCombinations;

	}

	public HashMap<String, String> getCacheResult(String key) {
		return cache.get(key);
	}

	public void addCacheResult(String key, HashMap<String, String> result) {
		cache.put(key, result);
	}

	// private String normalizeConfigString(String config) {
	// // Remove all whitespace
	// String cleaned = config.replaceAll("\\s+", "");

	// // Separate letters and numbers
	// StringBuilder letters = new StringBuilder();
	// ArrayList<BigInteger> numbers = new ArrayList<>();

	// StringBuilder numberBuffer = new StringBuilder();
	// for (char c : cleaned.toCharArray()) {
	// if (Character.isDigit(c)) {
	// numberBuffer.append(c);
	// } else {
	// // Flush any buffered number
	// if (numberBuffer.length() > 0) {
	// numbers.add(new BigInteger(numberBuffer.toString()));
	// numberBuffer.setLength(0);
	// }
	// letters.append(c);
	// }
	// }
	// // Flush any trailing number
	// if (numberBuffer.length() > 0) {
	// numbers.add(new BigInteger(numberBuffer.toString()));
	// }

	// // Sort letters and numbers
	// char[] letterArray = letters.toString().toCharArray();
	// Arrays.sort(letterArray);
	// numbers.sort(null); // Natural order for BigInteger

	// // Build normalized string
	// StringBuilder normalized = new StringBuilder();
	// normalized.append(letterArray);
	// for (BigInteger num : numbers) {
	// normalized.append(num);
	// }

	// return normalized.toString();
	// }

	/*
	 * Gets the directory of the project
	 */

	public String getDirectoryPath() {
		return this.directory;
	}

	public String getProjectFilePath() {
		return this.projectPath;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public boolean isConfigured() {
		return configured;
	}

	public ObservableList<SynthesisGoal> getAllSynthesisObjectives() {

		ObservableList<SynthesisGoal> synthesisParameters = FXCollections.observableArrayList();
		if (models.isEmpty()) {
			throw new IllegalStateException("No models found in the project.");
		}

		for (Model model : models) {
			synthesisParameters.addAll(model.getSynthesisGoals());
		}

		return synthesisParameters;

	}

	public ObservableList<InternalParameter> getAllInternalParameters() {

		ObservableList<InternalParameter> internalParameters = FXCollections.observableArrayList();
		if (models.isEmpty()) {
			throw new IllegalStateException("No models found in the project.");
		}

		for (Model model : models) {
			internalParameters.addAll(model.getInternalParameters());
		}

		return internalParameters;
	}

	public String generateParameterConfigurationKey() {
		ArrayList<Model> modelsArray = new ArrayList<>(models);
		modelsArray.sort(Comparator.comparing(Model::getModelId));

		StringBuilder sb = new StringBuilder();
		for (Model m : modelsArray) {
			sb.append("++" + m.getModelId() + "+");
			for (IParameter sp : m.getParameters()) {
				sb.append(sp.getConfigCacheString() + "::");
			}
		}

		return sb.toString().replace("\n", "");
	}

	public String generateParameterValuesKey() {
		ArrayList<Model> modelsArray = new ArrayList<>(models);
		modelsArray.sort(Comparator.comparing(Model::getModelId));

		StringBuilder sb = new StringBuilder();
		for (Model m : modelsArray) {
			sb.append("++" + m.getModelId() + "+");
			for (IParameter sp : m.getParameters()) {
				String value;
				try {
					value = sp.getValue();
				} catch (IOException e) {
					value = "VALUE_NOT_SET_CORRECTLY";
				}
				sb.append(sp.getConfigCacheString() + ":" + (value != null ? value : "VALUE_NOT_SET") + "::");
			}
		}

		return sb.toString().replace("\n", "");
	}

	public String generateVerificationCacheKey(Model vModel, Property vProp) {

		return generateVerificationCacheKey(vModel.getModelId(), vProp.getDefinition());

	}

	public String generateVerificationCacheKey(String modelId, String propertyDefinition) {

		String projectConfigKey = generateParameterValuesKey();
		return projectConfigKey + "+V:" + modelId + "+" + propertyDefinition;

	}

	public HashMap<String, List<InternalParameter>> getModelInternalParameterMap() {

		HashMap<String, List<InternalParameter>> map = new HashMap<>();

		for (Model m : models) {
			map.put(m.getModelId(), m.getInternalParameters());
		}

		return map;

	}

	public HashMap<String, String> getInternalParameterValuesMap() {
		HashMap<String, String> ipValues = new HashMap<>();
		for (Model m : models) {
			for (InternalParameter ip : m.getInternalParameters()) {
				ipValues.put(ip.getNameInModel(), ip.getValue());
			}
		}
		return ipValues;
	}

	public HashMap<String, String> getExternalParameterValues() {
		HashMap<String, String> epValues = new HashMap<>();
		for (Model m : models) {
			for (ExternalParameter ep : m.getExternalParameters()) {
				try {
					epValues.put(ep.getNameInModel(), ep.getValue());
				} catch (IOException e) {
					epValues.put(ep.getNameInModel(), "ERROR_GETTING_VALUE");
				}
			}
		}
		return epValues;
	}

	public HashMap<String, String> getRangedParameterUniqueIdValueMap() {
		HashMap<String, String> epValues = new HashMap<>();
		for (Model m : models) {
			for (ExternalParameter ep : m.getExternalParameters()) {
				if (ep instanceof RangedExternalParameter) {
					epValues.put(ParameterUtilities.generateUniqueParameterId(m.getModelId(), ep.getNameInModel()),
							((RangedExternalParameter) ep).getValue());
				}
			}
		}
		return epValues;
	}

	public HashMap<String, HashMap<String, String>> getRangedParameterValuesPerModel() {

		HashMap<String, HashMap<String, String>> modelValuesMap = new HashMap<>();
		for (Model m : models) {
			HashMap<String, String> epValues = new HashMap<>();
			for (ExternalParameter ep : m.getExternalParameters()) {
				if (ep instanceof RangedExternalParameter) {
					epValues.put(ep.getNameInModel(),
							((RangedExternalParameter) ep).getValue());
				}
			}
			modelValuesMap.put(m.getModelId(), epValues);
		}
		return modelValuesMap;
	}

	public HashMap<String, String> getDependencyParameterValues() {
		HashMap<String, String> dpValues = new HashMap<>();
		for (Model m : models) {
			for (DependencyParameter dp : m.getDependencyParameters()) {
				dpValues.put(dp.getNameInModel(), dp.getValue());
			}
		}
		return dpValues;
	}
	
	public void markAsModified(){
		if (!isModified){
			isModified = true;
			updateWindowTitle();
		}
	}
	
	public void markAsClean(){
		isModified = false;
		updateWindowTitle();
	}
	
	private void updateWindowTitle(){
		if (SharedContext.getMainStage() != null){
			String title = "Ultimate Multi-Model Verifier: " + projectName;
			if (isModified){
				title = "Ultimate Multi-Model Verifier: " + projectName + " *";
			}
			SharedContext.getMainStage().setTitle(title);
		}
	}
	
	public boolean isModified(){
		return isModified;
	}	

}
