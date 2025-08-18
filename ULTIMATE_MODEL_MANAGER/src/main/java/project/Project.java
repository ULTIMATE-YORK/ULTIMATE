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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import parameters.ExternalParameter;
import parameters.RangedExternalParameter;
import sharedContext.SharedContext;
import utils.Alerter;
//import utils.Alerter;
import utils.FileUtils;

public class Project {

	private static final Logger logger = LoggerFactory.getLogger(Project.class);

	private Set<Model> models; // set of models in the project
	private String projectPath;
	private ObservableList<Model> observableModels; // The observable list used for UI binding
	private ObjectProperty<Model> currentModel; // Observable property of current model
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
	private SharedContext sharedContext = SharedContext.getContext();
	private boolean isBlank;
	// key will be model id + property and second hashmap will be mapping of
	// configuration to result
	private HashMap<String, HashMap<String, Double>> cache = new HashMap<String, HashMap<String, Double>>(); // cache
																												// for
																												// storing
																												// results
																												// of
																												// generated
																												// models

	public Project(String projectPath) throws IOException {
		this.directory = getDirectory(projectPath);
		this.projectPath = projectPath;
		// sharedContext.setUltimateProject(this);
		FileUtils.isUltimateFile(projectPath); // throws IOE if file is not an ultimate project file
		importer = new ProjectImporter(projectPath);
		saveLocation = projectPath;
		models = importer.importProjectModels();
		// Initialise currentModel property (first model in the set or null)
		currentModel = new SimpleObjectProperty<>(models.isEmpty() ? null : models.iterator().next());
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
		this.currentModel = new SimpleObjectProperty<>(null);
		this.projectName = "untitled";
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
	}

	public void removeModel(Model removeModel) {
		// Remove from the set (by id comparison)
		models.removeIf(model -> model.getModelId().equals(removeModel.getModelId()));
		// Remove from the observable list
		observableModels.removeIf(model -> model.getModelId().equals(removeModel.getModelId()));
	}

	public String getProjectName() {
		return projectName;
	}

	public Set<Model> getModels() {
		return this.models;
	}

	// Getter for currentModel property (for binding)
	public ObjectProperty<Model> currentModelProperty() {
		return currentModel;
	}

	public void setCurrentModel(Model model) {
		this.currentModel.set(model);
	}

	public Model getCurrentModel() {
		return currentModel.get();
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
	}

	public void save() {
		exporter.saveExport(saveLocation);
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
		Model oldModel = getCurrentModel();
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
		File configFile = new File(System.getenv("ULTIMATE_DIR") + "/config.json");
		String content = new String(Files.readAllBytes(Paths.get(configFile.toURI())));
		JSONObject configJSON = new JSONObject(content);

		String stormInstall = configJSON.getString("stormInstall");
		if (FileUtils.isFile(stormInstall) && !stormInstall.equals("")) {
			this.stormInstall = stormInstall;
		} else {
			if (sharedContext.getMainStage() != null) {
				Alerter.showWarningAlert("No Storm Installation found!",
						"Please configure the location of the storm install on your system!");
				configured = false;
			}
		}

		String stormParsInstall = configJSON.getString("stormParsInstall");
		if (FileUtils.isFile(stormParsInstall) && !stormParsInstall.equals("")) {
			this.stormParsInstall = stormParsInstall;
		} else {
			if (sharedContext.getMainStage() != null) {
				Alerter.showWarningAlert("No Storm-Pars Installation found!",
						"Please configure the location of the storm-pars install on your system!");
				configured = false;
			}
		}

		String prismInstall = configJSON.getString("prismInstall");
		if (FileUtils.isFile(prismInstall) && !prismInstall.equals("")) {
			this.prismInstall = prismInstall;
		} else {
			if (sharedContext.getMainStage() != null) {
				Alerter.showWarningAlert("No PRISM Installation found!",
						"Please configure the location of the PRISM install on your system!");
				configured = false;
			}
		}

		String prismGamesInstall = configJSON.getString("prismGamesInstall");
		if (FileUtils.isFile(prismGamesInstall) && !prismGamesInstall.equals("")) {
			this.prismGamesInstall = prismGamesInstall;
		} else {
			if (sharedContext.getMainStage() != null) {
				Alerter.showWarningAlert("No PRISM games Installation found!",
						"Please configure the location of the PRISM Games install on your system!");
				configured = false;
			}
		}

		String pythonInstall = configJSON.getString("pythonInstall");
		if (FileUtils.isFile(pythonInstall) && !pythonInstall.equals("")) {
			this.pythonInstall = pythonInstall;
		} else {
			if (sharedContext.getMainStage() != null) {
				Alerter.showWarningAlert("No python Installation found!",
						"Please configure the location of the python install on your system!");
				configured = false;
			}
		}

	}

	// TODO Generate a temporary directory of evolable model files for evochecker
	public void generateEvoModels() {

	}

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

	public HashMap<String, ArrayList<String>> generateRangedExperimentConfiguration() {
		HashMap<String, ArrayList<String>> experimentConfiguration = new HashMap<>();

		for (Model m : models) {
			for (ExternalParameter ep : m.getExternalParameters()) {
				if (ep instanceof RangedExternalParameter) {
					experimentConfiguration.put(ep.getName(), ((RangedExternalParameter) ep).getValueOptions());
				}
			}
		}

		return experimentConfiguration;
	}

	public ArrayList<HashMap<String, String>> generateExperimentPlan() {

		// generate plan by combining every possible external parameter value with every
		// other
		HashMap<String, ArrayList<String>> experimentConfig = generateRangedExperimentConfiguration(); // key: parameter
																										// name, value:
																										// range

		ArrayList<HashMap<String, String>> experimentPlan = getAllCombinations(new ArrayList<HashMap<String, String>>(),
				experimentConfig, 0);

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
		ArrayList<HashMap<String, String>> allLowerCombinations = getAllCombinations(combinations, experimentConfig,
				depth + 1);
		ArrayList<HashMap<String, String>> newCombinations = new ArrayList<>();

		if (depth == experimentConfig.size()) {
			for (String value : thisDepthPropertyValues) {
				HashMap<String, String> bottomCombination = new HashMap<>();
				bottomCombination.put(propertyName, value);
				newCombinations.add(bottomCombination);
			}
		} else {
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

	public ArrayList<HashMap<Model, HashMap<String, String>>> generateModelResultsHashMap(ArrayList<Model> models) {
		ArrayList<HashMap<Model, HashMap<String, String>>> results = new ArrayList<>();
		backtrack(models, 0, new HashMap<>(), results);
		return results;
	}

	private void backtrack(ArrayList<Model> models, int index,
			HashMap<Model, HashMap<String, String>> current,
			ArrayList<HashMap<Model, HashMap<String, String>>> results) {
		if (index == models.size()) {
			results.add(new HashMap<>(current));
			return;
		}

		Model model = models.get(index);
		ArrayList<HashMap<String, String>> configs = model.getCartesianExternal();

		for (HashMap<String, String> config : configs) {
			current.put(model, config);
			backtrack(models, index + 1, current, results);
			// Optional: current.remove(model); // Not needed due to overwrite
		}
	}

	public Double getCacheResult(String verification, String config) {
		try {
			String normalizedConfig = normalizeConfigString(config);
			return cache.get(verification).get(normalizedConfig);
		} catch (Exception e) {
			return null;
		}
	}

	public void addCacheResult(String verification, String config, Double result) {
		String normalizedConfig = normalizeConfigString(config);
		try {
			cache.get(verification).put(normalizedConfig, result);
		} catch (Exception e) {
			HashMap<String, Double> newConfig = new HashMap<>();
			newConfig.put(normalizedConfig, result);
			cache.put(verification, newConfig);
		}
	}

	public void addCacheResult(String config, Double result) {
		// String normalizedConfig = normalizeConfigString(config);
		// try {
		// cache.get(verification).put(normalizedConfig, result);
		// } catch (Exception e) {
		// HashMap<String, Double> newConfig = new HashMap<>();
		// newConfig.put(normalizedConfig, result);
		// cache.put(verification, newConfig);
		// }
	}

	private String normalizeConfigString(String config) {
		// Remove all whitespace
		String cleaned = config.replaceAll("\\s+", "");

		// Separate letters and numbers
		StringBuilder letters = new StringBuilder();
		ArrayList<BigInteger> numbers = new ArrayList<>();

		StringBuilder numberBuffer = new StringBuilder();
		for (char c : cleaned.toCharArray()) {
			if (Character.isDigit(c)) {
				numberBuffer.append(c);
			} else {
				// Flush any buffered number
				if (numberBuffer.length() > 0) {
					numbers.add(new BigInteger(numberBuffer.toString()));
					numberBuffer.setLength(0);
				}
				letters.append(c);
			}
		}
		// Flush any trailing number
		if (numberBuffer.length() > 0) {
			numbers.add(new BigInteger(numberBuffer.toString()));
		}

		// Sort letters and numbers
		char[] letterArray = letters.toString().toCharArray();
		Arrays.sort(letterArray);
		numbers.sort(null); // Natural order for BigInteger

		// Build normalized string
		StringBuilder normalized = new StringBuilder();
		normalized.append(letterArray);
		for (BigInteger num : numbers) {
			normalized.append(num);
		}

		return normalized.toString();
	}

	/*
	 * Gets the directory of the project
	 */
	private String getDirectory(String projectPath) {
		Path path = Paths.get(projectPath);
		return path.toAbsolutePath().getParent().toString(); // Get directory
	}

	public String getDirectoryPath() {
		return this.directory;
	}

	public String getPath() {
		return this.projectPath;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public boolean isConfigured() {
		return configured;
	}
}
