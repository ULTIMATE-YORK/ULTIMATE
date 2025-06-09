package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Model;
import project.Project;
import property.Property;
import sharedContext.SharedContext;
import utils.Alerter;
import utils.DialogOpener;
import utils.FileUtils;
import utils.Font;
import verification.NPMCVerification;

public class PropertiesController {
	
	@FXML private Button addProperty;
	@FXML private Button scrollUp;
	@FXML private Button scrollDown;
	@FXML private Button verifyButton;
	@FXML private ListView<String> verifyResults;
	//@FXML private ProgressIndicator progressIndicator;
	@FXML private ListView<Property> propertyListView;
	@FXML private CheckBox showAllResults;
	@FXML private Button plotButton;
	@FXML private ChoiceBox<String> xaxisparam;
	@FXML private Button confirmPlotButton;
	@FXML private Button cancelPlotButton;
	
	private String currentModelId = null;
	private String currentProperty = null;
	
	private ObservableList<String> allVerificationResults = FXCollections.observableArrayList();
	private FilteredList<String> filteredVerificationResults = new FilteredList<>(allVerificationResults, s -> true);

	
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();
    
    private String verificationResult = "";
    private int verificationCount = 1;
    private int totalVerifications = 0; // updated later 
    
    private String xaxis = "";
	
	@FXML
	private void initialize() {
		if (project.getCurrentModel() != null) {
				propertyListView.setItems(project.getCurrentModel().getProperties());
		}
		verifyResults.setItems(filteredVerificationResults); // <--- set filtered list
		setCells();
		setListeners();
	}
	
	@FXML
	private void addProperty() throws IOException {
		if (project.getCurrentModel() == null) {
			Alerter.showErrorAlert("No Model Selected", "Select a model to add a property to!");
			return;
		}
		DialogOpener.openDialogWindow(sharedContext.getMainStage(), "/dialogs/add_property.fxml", "Add Property");
	}
	
	@FXML
	public void scrollUp() {
        int selectedIndex = propertyListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            // Select the item just above the current one
        	propertyListView.getSelectionModel().select(selectedIndex - 1);
        }
	}
	
	@FXML
	public void scrollDown() {
        int selectedIndex = propertyListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < propertyListView.getItems().size() - 1) {
            // Select the item just below the current one
        	propertyListView.getSelectionModel().select(selectedIndex + 1);
        }
	}
	
	@FXML
	private void verify() throws IOException {
	    verificationResult = "";
	    verificationCount = 1;

	    Model vModel = project.getCurrentModel();
	    Property vProp = propertyListView.getSelectionModel().getSelectedItem();

	    if (!validateSelection(vModel, vProp)) return;

	    Stage modalStage = createModalStage("Verification in Progress");

	    modalStage.show();

	    CompletableFuture.runAsync(() -> {
	        try {
	            if (project.containsRanged()) {
	                handleRangedVerification(vModel, vProp, modalStage);
	            } else {
	                handleSimpleVerification(vModel, vProp, modalStage);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            showVerificationError(modalStage);
	        }
	    });
	}
	
	@FXML
	private void plotResults() {
	    String selectedResult = verifyResults.getSelectionModel().getSelectedItem();
	    if (selectedResult == null || selectedResult.isEmpty()) return;

	    ParsedVerificationData parsedData = parseVerificationResult(selectedResult);

	    if (parsedData.configMaps.isEmpty() || parsedData.resultValues.isEmpty()) return;

	    // Handle single-parameter plot
	    if (parsedData.configMaps.get(0).size() == 1) {
	        plotSingleParameter(parsedData);
	    } else {
	        plotMultiParameter(parsedData);
	    }
	}
	
	@FXML
	private void confirmPlot() {
	    String selectedXaxis = xaxisparam.getSelectionModel().getSelectedItem();
	    if (selectedXaxis == null || selectedXaxis.isEmpty()) {
	        Alerter.showErrorAlert("Invalid Selection", "Please choose a parameter for the X-axis.");
	        return;
	    }

	    xaxis = selectedXaxis;

	    // Close the dialog window
	    Stage stage = (Stage) confirmPlotButton.getScene().getWindow();
	    stage.close();
	}
	
	@FXML 
	private void cancelPlot() {
		xaxis = "";
        Stage stage = (Stage) cancelPlotButton.getScene().getWindow();
        stage.close();
	}

	private boolean validateSelection(Model vModel, Property vProp) {
	    if (vModel == null || vProp == null) {
	        Alerter.showErrorAlert("CANNOT VERIFY", "Please select a model and a property to run verification on");
	        return false;
	    }
	    return true;
	}

	private Stage createModalStage(String title) {
	    Stage modalStage = new Stage();
	    modalStage.initModality(Modality.APPLICATION_MODAL);
	    modalStage.setTitle(title);

	    ProgressIndicator modalProgress = new ProgressIndicator();
	    modalProgress.setPrefSize(100, 100);
	    Scene modalScene = new Scene(modalProgress, 300, 200);
	    modalStage.setScene(modalScene);

	    return modalStage;
	}

	private void showVerificationError(Stage modalStage) {
	    Platform.runLater(() -> {
	        modalStage.close();
	        Alerter.showErrorAlert("Verification Failed", "There was an error communicating with the verification engine. Please run verification again.");
	    });
	}

	private void handleRangedVerification(Model vModel, Property vProp, Stage modalStage) throws Exception {
	    ArrayList<Model> models = new ArrayList<>(project.getModels());
	    models.sort(Comparator.comparing(Model::getModelId));

	    ArrayList<String> configurations = generateConfigurations(models);
	    String verificationKey = vModel.getModelId() + " + " + vProp.getProperty();

	    // Skip if any configuration is already cached
	    for (String config : configurations) {
	        if (project.getCacheResult(verificationKey, config) != null) {
	            Platform.runLater(modalStage::close);
	            return;
	        }
	    }

	    ArrayList<HashMap<Model, HashMap<String, Double>>> rounds = project.generate(models);
	    ExecutorService executor = Executors.newSingleThreadExecutor();

	    verificationResult += "Verification of " + vModel.getModelId() + " with property: " + vProp.getProperty() + "\n";
	    runVerificationsSequentially(rounds, 0, models, vModel.getModelId(), vProp.getProperty(), executor, modalStage);
	}

	private void handleSimpleVerification(Model vModel, Property vProp, Stage modalStage) throws IOException {
	    ArrayList<Model> models = new ArrayList<>(project.getModels());
	    models.sort(Comparator.comparing(Model::getModelId));

	    StringBuilder configBuilder = new StringBuilder();
	    for (Model m : models) {
	        configBuilder.append(m.toString());
	    }
	    configBuilder.append(vProp.getProperty());

	    String config = configBuilder.toString();
	    String verificationKey = vModel.getModelId() + " + " + vProp.getProperty();

	    if (project.getCacheResult(verificationKey, config) != null) {
	        Platform.runLater(modalStage::close);
	        return;
	    }

	    for (Model m : models) {
	        FileUtils.writeParametersToFile(m.getVerificationFilePath(), m.getHashExternalParameters());
	    }

	    NPMCVerification verifier = new NPMCVerification(models);
	    Double result = verifier.verify(vModel.getModelId(), vProp.getProperty());

	    Platform.runLater(() -> {
	        project.addCacheResult(verificationKey, config, result);
	        HashMap<String, Double> modelResults = new HashMap<>();
	        modelResults.put("DEFAULT", result);
	        vModel.addResult(vProp.getProperty(), modelResults);

	        verificationResult += "Verification of " + vModel.getModelId() + " with property: " + vProp.getProperty() + "\nResult: " + result + "\n";

	        modalStage.close();
	        addVerificationResult(verificationResult);
	    });
	}
	
	// Simple container for parsed verification results
	private static class ParsedVerificationData {
	    List<HashMap<String, Double>> configMaps;
	    List<Double> resultValues;

	    ParsedVerificationData(List<HashMap<String, Double>> configMaps, List<Double> resultValues) {
	        this.configMaps = configMaps;
	        this.resultValues = resultValues;
	    }
	}
	
	private ParsedVerificationData parseVerificationResult(String resultText) {
	    List<HashMap<String, Double>> configMaps = new ArrayList<>();
	    List<Double> resultValues = new ArrayList<>();

	    String[] lines = resultText.split("\\r?\\n");
	    HashMap<String, Double> currentConfig = null;
	    boolean inConfigSection = false;

	    for (String line : lines) {
	        line = line.trim();

	        if (line.startsWith("Verification") && line.contains("of") && !line.contains("property")) {
	            currentConfig = new HashMap<>();
	            inConfigSection = false;
	        } else if (line.startsWith("Configuration:")) {
	            inConfigSection = true;
	        } else if (line.startsWith("Result:")) {
	            if (currentConfig != null) {
	                try {
	                    double result = Double.parseDouble(line.substring("Result:".length()).trim());
	                    resultValues.add(result);
	                    configMaps.add(currentConfig);
	                } catch (NumberFormatException ignored) {}
	            }
	            inConfigSection = false;
	        } else if (inConfigSection && currentConfig != null && line.contains(":")) {
	            String[] parts = line.split(":", 2);
	            if (parts.length == 2) {
	                try {
	                    currentConfig.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
	                } catch (NumberFormatException ignored) {}
	            }
	        }
	    }

	    return new ParsedVerificationData(configMaps, resultValues);
	}
	
	private void plotSingleParameter(ParsedVerificationData data) {
	    String configKey = data.configMaps.get(0).keySet().iterator().next();

	    NumberAxis xAxis = new NumberAxis();
	    NumberAxis yAxis = new NumberAxis();
	    xAxis.setLabel(configKey);
	    yAxis.setLabel("Result");

	    LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
	    lineChart.setTitle(currentModelId + " - " + currentProperty);

	    XYChart.Series<Number, Number> series = new XYChart.Series<>();
	    series.setName(configKey + " vs Result");

	    for (int i = 0; i < data.configMaps.size(); i++) {
	        double x = data.configMaps.get(i).get(configKey);
	        double y = data.resultValues.get(i);
	        series.getData().add(new XYChart.Data<>(x, y));
	    }

	    lineChart.getData().add(series);
	    showChart(lineChart, "Plot of Results");
	}
	
	private void plotMultiParameter(ParsedVerificationData data) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dialogs/chooseXAxisPlot.fxml"));
	        loader.setController(this);
	        Pane root = loader.load();

	        if (!data.configMaps.isEmpty()) {
	            HashMap<String, Double> firstMap = data.configMaps.get(0);
	            ObservableList<String> keys = FXCollections.observableArrayList(firstMap.keySet());
	            xaxisparam.setItems(keys);
	        }

	        Stage dialogStage = new Stage();
	        dialogStage.initModality(Modality.APPLICATION_MODAL);
	        dialogStage.setTitle("Select X-Axis Parameter");
	        dialogStage.setScene(new Scene(root));
	        dialogStage.showAndWait();

	        xaxis = xaxisparam.getSelectionModel().getSelectedItem();

	        if (xaxis != null && !xaxis.isEmpty()) {
	            plotGroupedByRemainingParams(data);
	        }

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	private void plotGroupedByRemainingParams(ParsedVerificationData data) {
	    Map<String, XYChart.Series<Number, Number>> seriesMap = new HashMap<>();

	    for (int i = 0; i < data.configMaps.size(); i++) {
	        HashMap<String, Double> config = data.configMaps.get(i);
	        Double result = data.resultValues.get(i);
	        if (result == null || !config.containsKey(xaxis)) continue;

	        double xVal = config.get(xaxis);
	        StringBuilder labelBuilder = new StringBuilder();

	        for (Map.Entry<String, Double> entry : config.entrySet()) {
	            if (!entry.getKey().equals(xaxis)) {
	                if (labelBuilder.length() > 0) labelBuilder.append(", ");
	                labelBuilder.append(entry.getKey()).append("=").append(entry.getValue());
	            }
	        }

	        String label = labelBuilder.length() > 0 ? labelBuilder.toString() : "default";

	        XYChart.Series<Number, Number> series = seriesMap.computeIfAbsent(label, l -> {
	            XYChart.Series<Number, Number> s = new XYChart.Series<>();
	            s.setName(l);
	            return s;
	        });

	        series.getData().add(new XYChart.Data<>(xVal, result));
	    }

	    List<XYChart.Series<Number, Number>> allSeries = new ArrayList<>(seriesMap.values());
	    int chunkSize = 3;

	    for (int i = 0; i < allSeries.size(); i += chunkSize) {
	        NumberAxis xAxisObj = new NumberAxis();
	        NumberAxis yAxisObj = new NumberAxis();
	        xAxisObj.setLabel(xaxis);
	        yAxisObj.setLabel("Result");

	        LineChart<Number, Number> lineChart = new LineChart<>(xAxisObj, yAxisObj);
	        lineChart.setTitle(currentModelId + " - " + currentProperty);

	        for (int j = i; j < Math.min(i + chunkSize, allSeries.size()); j++) {
	            lineChart.getData().add(allSeries.get(j));
	        }

	        showChart(lineChart, "Plot of Results (" + (i + 1) + " to " + (Math.min(i + chunkSize, allSeries.size())) + ")");
	    }
	}

	private void showChart(LineChart<Number, Number> chart, String title) {
	    Pane chartPane = new Pane();
	    chartPane.getChildren().add(chart);
	    Scene scene = new Scene(chartPane, 500, 400);
	    Stage stage = new Stage();
	    stage.setTitle(title);
	    stage.setScene(scene);
	    stage.show();
	}
	
	private String buildConfigString(List<Model> models) {
	    StringBuilder configBuilder = new StringBuilder();

	    for (Model model : models) {
	        configBuilder.append(model.toString());
	    }

	    return configBuilder.toString();
	}
	
	private void setListeners() {
	    // Model change listener
	    project.currentModelProperty().addListener((obs, oldModel, newModel) -> {
	        Platform.runLater(() -> {
	            if (newModel != null) {
	                propertyListView.setItems(newModel.getProperties());
	                currentModelId = newModel.getModelId();
	            } else {
	                currentModelId = null;
	            }
	            updateVerifyFilter(); // <- apply updated filtering
	        });
	    });

	    // Property selection change listener
	    propertyListView.getSelectionModel().selectedItemProperty().addListener((obs, oldProperty, newProperty) -> {
	        Platform.runLater(() -> {
	            if (newProperty != null) {
	                currentProperty = newProperty.getProperty();
	            } else {
	                currentProperty = null;
	            }
	            updateVerifyFilter(); // <- apply updated filtering
	        });
	    });
	    
	    verifyResults.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
	        plotButton.setVisible(hasMultipleResults(newVal));
	    });
	    
		// Add checkbox listener
		showAllResults.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
			updateVerifyFilter();
		});
	    verifyResults.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
	        plotButton.setVisible(hasMultipleResults(newVal));
	    });

	}
	
	private void setCells() {
		propertyListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Property item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label label = new Label(item.getProperty()); // Display the model ID
                    label.setStyle(Font.UC_LIST_FONT); // Apply font styling
                    setGraphic(label); // Set the label as the cell's graphic
                    setText(null); // Clear any text (not needed with graphic)
                }
            }
        });
		
        // Add a key event handler to allow deletion of models using the Backspace key
		propertyListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                Property selectedProperty = propertyListView.getSelectionModel().getSelectedItem();
                if (selectedProperty != null) {
                    project.getCurrentModel().removeProperty(selectedProperty); // Remove the selected property
                }
            }
        });
		
		//verifyResults.setCellFactory(null);
	}	

	private void runVerificationsSequentially(
	        List<HashMap<Model, HashMap<String, Double>>> rounds,
	        int index,
	        ArrayList<Model> models,
	        String verifyModelId,
	        String property,
	        ExecutorService executor,
	        Stage modalStage) {
	
	    AtomicReference<String> ep = new AtomicReference<>("");
	    totalVerifications = rounds.size();
	
	    if (index >= rounds.size()) {
	        executor.shutdown();
	        Platform.runLater(() -> {
	            modalStage.close(); // Close the modal window here
	            addVerificationResult(verificationResult);
	            //updateVerifyResults(); // Update the results
	        });
	        return;
	    }
	
	    HashMap<Model, HashMap<String, Double>> round = rounds.get(index);
	    String ep_config = generateKeyValueString(round);
	    String epConfig = "";
	
	    // Apply parameter values and write to files
	    for (Model m : round.keySet()) {
	        HashMap<String, Double> parameters = round.get(m);
	        for (Map.Entry<String, Double> entry : parameters.entrySet()) {
	            if (entry.getValue() != null) {
	                m.getExternalParameter(entry.getKey()).setValue(entry.getValue());
	                epConfig += entry.getKey() + " : " + entry.getValue() + "\n";
	            }
	        }
	        try {
				FileUtils.writeParametersToFile(m.getVerificationFilePath(), parameters);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	
	    ep.set(epConfig);
	
	    NPMCVerification verifier = new NPMCVerification(models);
	
	    // Run verification asynchronously
	    CompletableFuture
	        .supplyAsync(() -> {
	            try {
	                return verifier.verify(verifyModelId, property);
	            } catch (IOException e) {
	                e.printStackTrace();
	                return null;
	            }
	        }, executor)
	        .thenAccept(result -> {
	            Platform.runLater(() -> {
	                if (result != null) {
	                    String verification = verifyModelId + " + " + property;
	                    String config = buildConfigString(models);
	                    config += ep;
	                    project.addCacheResult(verification, config, result);
	
	                    Model model = models.stream()
	                        .filter(m -> m.getModelId().equals(verifyModelId))
	                        .findFirst()
	                        .orElse(null);
	                    HashMap<String, Double> modelResults = new HashMap<>();
	                    modelResults.put(ep_config, result);
	                    verificationResult += "\nVerification " + verificationCount + " of " + totalVerifications + "\nConfiguration:\n" + ep_config + "\nResult: " + result + "\n";
	                    verificationCount++;
	                    model.addResult(property, modelResults);
	                } else {
	                	modalStage.close(); // Ensure modal is closed on error
	                    Alerter.showErrorAlert("Verification Failed", "There was an error communicating with the verification engine. Please run verification again.");
	                }
	            });
	
	            runVerificationsSequentially(rounds, index + 1, models, verifyModelId, property, executor, modalStage);
	        })
	        .exceptionally(ex -> {
	            ex.printStackTrace();
	            Platform.runLater(() -> {
	                modalStage.close(); // Ensure modal is closed on error
	                Alerter.showErrorAlert("Verification Failed", "AThere was an error communicating with the verification engine. Please run verification again");
	            });
	            return null;
	        });
	}
	
	private ArrayList<String> generateConfigurations(List<Model> models) {
	    ArrayList<String> configs = new ArrayList<>();
	    ArrayList<ArrayList<String>> allModelStrings = new ArrayList<>();

	    // Prepare nested list
	    for (Model m : models) {
	        if (m.isRangedModel()) {
	            allModelStrings.add(m.rangedToString());
	        } else {
	            ArrayList<String> single = new ArrayList<>();
	            single.add(m.toString());
	            allModelStrings.add(single);
	        }
	    }

	    // Start recursive combination
	    generatePermutations(allModelStrings, 0, new StringBuilder(), configs);
	    return configs;
	}
	
	private void generatePermutations(List<ArrayList<String>> allModelStrings, int depth, StringBuilder current, List<String> result) {
	    if (depth == allModelStrings.size()) {
	        result.add(current.toString());
	        return;
	    }

	    ArrayList<String> currentList = allModelStrings.get(depth);
	    for (String s : currentList) {
	        int originalLength = current.length();
	        current.append(s);
	        generatePermutations(allModelStrings, depth + 1, current, result);
	        current.setLength(originalLength); // backtrack
	    }
	}

	private String generateKeyValueString(HashMap<Model, HashMap<String, Double>> round) {
	    StringBuilder result = new StringBuilder();
	
	    for (Map.Entry<Model, HashMap<String, Double>> modelEntry : round.entrySet()) {
	        HashMap<String, Double> parameters = modelEntry.getValue();
	        for (Map.Entry<String, Double> paramEntry : parameters.entrySet()) {
	            result.append(paramEntry.getKey())
	                  .append(" : ")
	                  .append(paramEntry.getValue())
	                  .append("\n");
	        }
	    }
	
	    return result.toString().trim(); // Remove the trailing newline
	}
	
	private void addVerificationResult(String result) {
		Platform.runLater(() -> {
	        allVerificationResults.add(result); // <-- add to backing list
		});
	}
	
	private void updateVerifyFilter() {
	    if (showAllResults != null && showAllResults.isSelected()) {
	        // Show all results
	        filteredVerificationResults.setPredicate(s -> true);
	    } else {
	        // Filter by current model and property
	        filteredVerificationResults.setPredicate(result -> {
	            boolean matchesModel = (currentModelId == null || result.contains(currentModelId));
	            boolean matchesProperty = (currentProperty == null || result.contains(currentProperty));
	            return matchesModel && matchesProperty;
	        });
	    }
	}
	
	private boolean hasMultipleResults(String text) {
	    if (text == null || text.isEmpty()) return false;

	    Pattern pattern = Pattern.compile("Verification\\s+\\d+\\s+of\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(text);
	    if (matcher.find()) {
	        try {
	            int total = Integer.parseInt(matcher.group(1));
	            return total > 1;
	        } catch (NumberFormatException e) {
	            return false;
	        }
	    }
	    return false;
	}
}
