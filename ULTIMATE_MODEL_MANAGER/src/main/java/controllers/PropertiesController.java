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
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyCode;
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
	
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();
	
	@FXML
	private void initialize() {
		if (project.getCurrentModel() != null) {
				propertyListView.setItems(project.getCurrentModel().getProperties());
		}
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
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> Alerter.showErrorAlert("Verification Failed", "Check the logs for the reason of failure"));
        } finally {
            // Close the modal window on completion
            //Platform.runLater(modalStage::close);
            Platform.runLater(this::updateVerifyResults);
        }
    });
}

	
	private String buildConfigString(List<Model> models) {
	    StringBuilder configBuilder = new StringBuilder();

	    for (Model model : models) {
	        configBuilder.append(model.toString());
	    }

	    return configBuilder.toString();
	}

	
	private void setListeners() {
        project.currentModelProperty().addListener((obs, oldModel, newModel) -> {
            if (newModel != null) {
                // Retrieve the list of Uncategorised Parameters from the new model.
                Platform.runLater(() -> {
                    propertyListView.setItems(newModel.getProperties());
                    updateVerifyResults();
                });
            }
        });
        // Add listener for propertyListView selection changes
        propertyListView.getSelectionModel().selectedItemProperty().addListener((obs, oldProperty, newProperty) -> {
            if (newProperty != null) {
                Platform.runLater(() -> {
                    updateVerifyResults();
                });
            }
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
	        ExecutorService executor, Stage modalStage) throws IOException {
		
		AtomicReference<String> ep = new AtomicReference<>("");

	    if (index >= rounds.size()) {
	        //Platform.runLater(() -> progressIndicator.setVisible(false));
	        executor.shutdown();
	        Platform.runLater(() -> {
	            modalStage.close(); // Close the modal window
	            updateVerifyResults(); // Update the results
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
	                //ep.set("\n" + entry.getKey() + " : " + entry.getValue() + "\n");
	            }
	        }
	        FileUtils.writeParametersToFile(m.getVerificationFilePath(), parameters);
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
	                    //verifyResults.appendText("Result for model: {" + verifyModelId + "} with property: {" + property + "}\nResult: " + result + "\n");
	                    String verification = verifyModelId + " + " + property;
	                    String config = buildConfigString(models);
	                    config += ep;
	                    //System.out.println("Cacheing: "  + config + "\n");
	                    project.addCacheResult(verification, config, result);
	                    // add result to model
	                    Model model = models.stream().filter(m -> m.getModelId().equals(verifyModelId)).findFirst().orElse(null);
	                    HashMap<String, Double> modelResults = new HashMap<>();
	                    modelResults.put(ep_config, result);
	                    model.addResult(property, modelResults);
	                } else {
	                    //verifyResults.appendText("Verification failed for model: {" + verifyModelId + "} with property: {" + property + "}\n");
	                	// TODO set results
	                    Alerter.showErrorAlert("Verification Failed", "Check the logs for the reason of failure");
	                }
	            });

	            // Proceed to next round
	            try {
					runVerificationsSequentially(rounds, index + 1, models, verifyModelId, property, executor, modalStage);
				} catch (IOException e) {
					e.printStackTrace();
				}
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
	
	private void updateVerifyResults() {
	    verifyResults.getItems().clear(); // Clear existing items
		Model m = project.getCurrentModel();
		String property = "";
		try {
			property = propertyListView.getSelectionModel().getSelectedItem().getProperty();
		} catch (Exception e) {
			return;
		}
		HashMap<String, Double> results = m.getResultMap(property);
		if (results == null) {
			return;
		}
		int i = 1;
		int total = results.size();
		for (String key : results.keySet()) {
			if (key.equals("DEFAULT")) {
	            verifyResults.getItems().add("Result of verification on model: " + m.getModelId() + " with property: " + property + "\nResult: " + results.get(key));
			}
			else {
	            verifyResults.getItems().add("Verification " + i + " of " + total + "\n" + key + "\nResult: " + results.get(key));
			}
		}
	}

}
