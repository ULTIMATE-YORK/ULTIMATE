package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
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
		// Add checkbox listener
		showAllResults.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
			updateVerifyFilter();
		});
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
	
	@FXML
	private void verify() throws IOException {
		verificationResult = ""; // reset the string
		verificationCount = 1; // reset the count
	    Model vModel = project.getCurrentModel();
	    Property vProp = propertyListView.getSelectionModel().getSelectedItem();
	    if (vModel == null || vProp == null) {
	        Alerter.showErrorAlert("CANNOT VERIFY", "Please select a model and a property to run verification on");
	        return;
	    }
	
	    // Create a modal window
	    Stage modalStage = new Stage();
	    modalStage.initModality(Modality.APPLICATION_MODAL);
	    modalStage.setTitle("Verification in Progress");
	
	    // Add a progress indicator to the modal
	    ProgressIndicator modalProgress = new ProgressIndicator();
	    modalProgress.setPrefSize(100, 100);
	    Scene modalScene = new Scene(modalProgress, 300, 200);
	    modalStage.setScene(modalScene);
	
	    // Show the modal window
	    modalStage.show();
	
	    // Run the verification process in a background thread
	    CompletableFuture.runAsync(() -> {
	        try {
	            if (project.containsRanged()) {
	                boolean cont = true;
	                ArrayList<Model> models = new ArrayList<>(project.getModels());
	                models.sort((m1, m2) -> m1.getModelId().compareToIgnoreCase(m2.getModelId()));
	                ArrayList<String> configurations = generateConfigurations(models);
	
	                for (String config : configurations) {
	                    String verification = vModel.getModelId() + " + " + vProp.getProperty();
	                    if (project.getCacheResult(verification, config) != null) {
	                        Platform.runLater(() ->  {
	                        	modalStage.close(); // Ensure modal is closed on error
	                        });
	                        cont = false;
	                    }
	                }
	                if (!cont) {
	                    return;
	                }
	                
	                //boolean continueVerification = Alerter.showConfirmationAlert("Ranged Parameters Detected", "This model contains ranged parameters. Do you want to continue verification?");
	                if (true) {
	                    ArrayList<HashMap<Model, HashMap<String, Double>>> rounds = project.generate(models);
	                    ExecutorService executor = Executors.newSingleThreadExecutor();
	                    verificationResult += "Verification of " + vModel.getModelId() + " with property: " + vProp.getProperty() + "\n";
	                    runVerificationsSequentially(rounds, 0, models, vModel.getModelId(), vProp.getProperty(), executor, modalStage);
	                }
	            } else {
	                ArrayList<Model> models = new ArrayList<>(project.getModels());
	                models.sort((m1, m2) -> m1.getModelId().compareToIgnoreCase(m2.getModelId()));
	                StringBuilder configBuilder = new StringBuilder();
	                for (Model m : models) {
	                    configBuilder.append(m.toString());
	                }
	                configBuilder.append(vProp.getProperty());
	                String config = configBuilder.toString();
	                String verification = vModel.getModelId() + " + " + vProp.getProperty();
	                if (project.getCacheResult(verification, config) != null) {
	                    Platform.runLater(() ->  {
	                    	modalStage.close(); // Ensure modal is closed on error
	                    });
	                    return;
	                }
	                for (Model m : models) {
	                    FileUtils.writeParametersToFile(m.getVerificationFilePath(), m.getHashExternalParameters());
	                }
	                NPMCVerification verifier = new NPMCVerification(models);
	                Double result = verifier.verify(vModel.getModelId(), vProp.getProperty());
	                Platform.runLater(() -> {
	                    project.addCacheResult(verification, config, result);
	                    HashMap<String, Double> modelResults = new HashMap<>();
	                    modelResults.put("DEFAULT", result);
	                    vModel.addResult(vProp.getProperty(), modelResults);
	                    verificationResult += "Verification of " + vModel.getModelId() + " with property: " + vProp.getProperty() + "\nResult: " + result + "\n";
	                });
	                //updateVerifyResults();
	                Platform.runLater( () -> {
	                	modalStage.close(); // Close the modal window after verification
	                	addVerificationResult(verificationResult);
	                });            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            Platform.runLater(() ->  {
	            	Alerter.showErrorAlert("Verification Failed", "Check the logs for the reason of failure");
	            	modalStage.close(); // Ensure modal is closed on error
	            });
	        } finally {
	            // Close the modal window on completion
	            //updateVerifyResults();
	        }
	    });
	}

	@FXML
	private void plotResults() {
	    String selectedResult = verifyResults.getSelectionModel().getSelectedItem();
	    if (selectedResult == null || selectedResult.isEmpty()) return;

	    List<HashMap<String, Double>> configMaps = new ArrayList<>();
	    List<Double> resultValues = new ArrayList<>();

	    // Split by lines and parse
	    String[] lines = selectedResult.split("\\r?\\n");
	    HashMap<String, Double> currentConfig = null;
	    boolean inConfigSection = false;

	    for (String line : lines) {
	        line = line.trim();

	        // Start of a new verification block
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
	                } catch (NumberFormatException e) {
	                    e.printStackTrace(); // or alert if needed
	                }
	            }
	            inConfigSection = false;
	        } else if (inConfigSection && currentConfig != null && line.contains(":")) {
	            String[] parts = line.split(":", 2);
	            if (parts.length == 2) {
	                try {
	                    String key = parts[0].trim();
	                    double value = Double.parseDouble(parts[1].trim());
	                    currentConfig.put(key, value);
	                } catch (NumberFormatException e) {
	                    e.printStackTrace(); // or skip invalid lines
	                }
	            }
	        }
	    }

	    // For testing/logging output
	    //System.out.println("Configurations: " + configMaps);
	    //System.out.println("Results: " + resultValues);

	    // Check if each config only contains one parameter
	    if (!configMaps.isEmpty() && configMaps.get(0).size() == 1) {
	        // Get the single config parameter key (assumes all maps have the same key)
	        String configKey = configMaps.get(0).keySet().iterator().next();

	        // Define axes
	        NumberAxis xAxis = new NumberAxis();
	        NumberAxis yAxis = new NumberAxis();
	        xAxis.setLabel(configKey);
	        yAxis.setLabel("Result");

	        // Create line chart
	        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
	        lineChart.setTitle(currentModelId + " - " + currentProperty);

	        // Prepare series
	        XYChart.Series<Number, Number> series = new XYChart.Series<>();
	        series.setName(configKey + " vs Result");

	        for (int i = 0; i < configMaps.size(); i++) {
	            double xValue = configMaps.get(i).get(configKey);
	            double yValue = resultValues.get(i);
	            series.getData().add(new XYChart.Data<>(xValue, yValue));
	        }

	        lineChart.getData().add(series);
	    	Pane chartPane = new Pane();
	        // Replace previous chart (optional: clear old chart if needed)
	        chartPane.getChildren().clear();
	        chartPane.getChildren().add(lineChart);
	        Scene scene = new Scene(chartPane, 500, 400);
	        Stage stage = new Stage();
	        stage.setScene(scene);
	        stage.show();
	    }
	    // when there is multiple external params
	    else {
	    	// opens a dialog that allows the user to choose which external parameter to use as x-axis
	    	try {
	    	    // Load the FXML manually
	    	    FXMLLoader loader = new FXMLLoader(getClass().getResource("/dialogs/chooseXAxisPlot.fxml"));

	    	    // Use the current controller
	    	    loader.setController(this);

	    	    // Load the parent node
	    	    Pane root = loader.load();

	    	    // Use first HashMap to populate xaxisparam
	    	    if (!configMaps.isEmpty()) {
	    	        HashMap<String, Double> firstMap = configMaps.get(0);
	    	        ObservableList<String> keys = FXCollections.observableArrayList(firstMap.keySet());
	    	        xaxisparam.setItems(keys); // 'xaxisparam' is your @FXML ChoiceBox<String>
	    	    }

	    	    // Show the dialog
	    	    Stage dialogStage = new Stage();
	    	    dialogStage.initModality(Modality.APPLICATION_MODAL);
	    	    dialogStage.setTitle("Plot Results");
	    	    dialogStage.setScene(new Scene(root));
	    	    dialogStage.showAndWait();
	    	    
	    	    if (xaxis != null && !xaxis.isEmpty()) {
	    	        Map<String, XYChart.Series<Number, Number>> lineMap = new HashMap<>();

	    	        // Build the map of label -> series with all points first
	    	        for (int i = 0; i < configMaps.size(); i++) {
	    	            HashMap<String, Double> config = configMaps.get(i);
	    	            Double result = resultValues.get(i);
	    	            if (result == null || !config.containsKey(xaxis)) continue;

	    	            Double xValue = config.get(xaxis);

	    	            // Build label using all params EXCEPT the selected xaxis
	    	            StringBuilder labelBuilder = new StringBuilder();
	    	            for (Map.Entry<String, Double> entry : config.entrySet()) {
	    	                if (!entry.getKey().equals(xaxis)) {
	    	                    if (labelBuilder.length() > 0) labelBuilder.append(", ");
	    	                    labelBuilder.append(entry.getKey()).append("=").append(entry.getValue());
	    	                }
	    	            }
	    	            String label = labelBuilder.toString();
	    	            if (label.isEmpty()) label = "default";

	    	            XYChart.Series<Number, Number> series = lineMap.computeIfAbsent(label, k -> {
	    	                XYChart.Series<Number, Number> s = new XYChart.Series<>();
	    	                s.setName(k);
	    	                return s;
	    	            });

	    	            series.getData().add(new XYChart.Data<>(xValue, result));
	    	        }

	    	        // Now split series into chunks of 3 and create separate charts/windows
	    	        List<XYChart.Series<Number, Number>> allSeries = new ArrayList<>(lineMap.values());

	    	        int chunkSize = 3;
	    	        for (int i = 0; i < allSeries.size(); i += chunkSize) {
	    	            NumberAxis xAxisObj = new NumberAxis();
	    	            NumberAxis yAxisObj = new NumberAxis();
	    	            xAxisObj.setLabel(xaxis);
	    	            yAxisObj.setLabel("Result");

	    	            LineChart<Number, Number> lineChart = new LineChart<>(xAxisObj, yAxisObj);
	    	            lineChart.setTitle(currentModelId + " - " + currentProperty);

	    	            int end = Math.min(i + chunkSize, allSeries.size());
	    	            for (int j = i; j < end; j++) {
	    	                lineChart.getData().add(allSeries.get(j));
	    	            }

	    	            Pane chartPane = new Pane();
	    	            chartPane.getChildren().add(lineChart);
	    	            Scene scene = new Scene(chartPane, 500, 400);
	    	            Stage stage = new Stage();
	    	            stage.setScene(scene);
	    	            stage.setTitle("Plot of Results (Lines " + (i + 1) + " to " + end + ")");
	    	            stage.show();
	    	        }
	    	    }


	    	} catch (IOException e) {
	    	    e.printStackTrace();
	    	}


	    }

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
	    
	    plotButton.visibleProperty().bind(
	    	    verifyResults.getSelectionModel().selectedItemProperty().isNotNull()
	    	);

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
	                    Alerter.showErrorAlert("Verification Failed", "Check the logs for the reason of failure");
	                }
	            });
	
	            runVerificationsSequentially(rounds, index + 1, models, verifyModelId, property, executor, modalStage);
	        })
	        .exceptionally(ex -> {
	            ex.printStackTrace();
	            Platform.runLater(() -> {
	                Alerter.showErrorAlert("Verification Error", "An error occurred during verification. Check logs for details.");
	                modalStage.close(); // Ensure modal is closed on error
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



}
