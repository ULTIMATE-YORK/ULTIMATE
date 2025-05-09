package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
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
	@FXML private TextArea verifyResults;
	@FXML private ProgressIndicator progressIndicator;
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
		//DialogOpener.openDialogWindow(sharedContext.getMainStage(), "/dialogs/verify_dialog.fxml", "Verification");
		Model vModel = project.getCurrentModel();
		Property vProp = propertyListView.getSelectionModel().getSelectedItem();
		if (vModel == null || vProp == null) {
			Alerter.showErrorAlert("CANNOT VERIFY", "Please select a model and a property to run verification on");
			return;
		}
		else {
			
			if (project.containsRanged()) {
				// check the cache
			    ArrayList<Model> models = new ArrayList<>(project.getModels());
			    models.sort((m1, m2) -> m1.getModelId().compareToIgnoreCase(m2.getModelId()));
			    StringBuilder configBuilder = new StringBuilder();
			    for (Model m : models) {
			        configBuilder.append(m.toString());
			    }
			    configBuilder.append(vProp.getProperty());
			    String config = configBuilder.toString();
				if (project.getCacheResult(config) != null) {
					// show the result from the cache
					verifyResults.setText("Result for model: {" + vModel.getModelId() + "} with property: {" + vProp.getProperty() + "}\nResult: " + project.getCacheResult(config));
					return;
				}
				boolean continueVerification = Alerter.showConfirmationAlert("Ranged Parameters Detected", "This model contains ranged parameters. Do you want to continue verification?");
				if (continueVerification) {
				    ArrayList<HashMap<Model, HashMap<String, Double>>> rounds = project.generate(models);

				    // Make the progress indicator visible and clear previous results
				    progressIndicator.setVisible(true);
				    verifyResults.setText("Verification in progress...\n");

				    // Start verification using an executor
				    ExecutorService executor = Executors.newSingleThreadExecutor();
				    runVerificationsSequentially(rounds, 0, models, vModel.getModelId(), vProp.getProperty(), executor);
				}
			}

			
			else {
				ArrayList<Model> models = new ArrayList<>();
				models.addAll(project.getModels());
				models.sort((m1, m2) -> m1.getModelId().compareToIgnoreCase(m2.getModelId()));
				StringBuilder configBuilder = new StringBuilder();
				for (Model m : models) {
				    configBuilder.append(m.toString());
				}
				configBuilder.append(vProp.getProperty());
				String config = configBuilder.toString();
				if (project.getCacheResult(config) != null) {
					// show the result from the cache
					verifyResults.setText("Result for model: {" + vModel.getModelId() + "} with property: {" + vProp.getProperty() + "}\nResult: " + project.getCacheResult(config));
					return;
				}
				// update the mode files here
				for (Model m : models) {
					FileUtils.writeParametersToFile(m.getVerificationFilePath(), m.getHashExternalParameters());
					//System.out.println("File: " + m.getVerificationFilePath() + "\nPrams: " + m.getHashExternalParameters() + "\n" + Files.readString(Paths.get(m.getVerificationFilePath())));
				}
				NPMCVerification verifier = new NPMCVerification(models);
			    
				// Show the loading spinner and update the text area message
			    progressIndicator.setVisible(true);
			    verifyResults.setText("Verification in progress...");
				
			    CompletableFuture.supplyAsync(() -> {
					try {
						return verifier.verify(vModel.getModelId(), vProp.getProperty());
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				})
			    .thenAccept(result -> Platform.runLater(() -> {
			        progressIndicator.setVisible(false);
			        verifyResults.setText("Result for model: {" + vModel.getModelId() + "} with property: {" + vProp.getProperty() + "}\nResult: " + result);
			        // Cache the result
			        project.addCacheResult(config, result);
			    }))
			    .exceptionally(ex -> {
			        Platform.runLater(() -> {
			            progressIndicator.setVisible(false);
				        verifyResults.setText(""); 
			            Alerter.showErrorAlert("Verification Failed", "Check the logs for the reason of failure" );
			        });
			        return null;
			    });	
			}
			

		}
	}
	
	private void setListeners() {
        project.currentModelProperty().addListener((obs, oldModel, newModel) -> {
            if (newModel != null) {
                // Retrieve the list of Uncategorised Parameters from the new model.
                Platform.runLater(() -> {
                    propertyListView.setItems(newModel.getProperties());
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
		
		//TODO: make this OS independent
        // Add a key event handler to allow deletion of models using the Backspace key
		propertyListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                Property selectedProperty = propertyListView.getSelectionModel().getSelectedItem();
                if (selectedProperty != null) {
                    project.getCurrentModel().removeProperty(selectedProperty); // Remove the selected property
                }
            }
        });
	}
	
	private void runVerificationsSequentially(
	        List<HashMap<Model, HashMap<String, Double>>> rounds,
	        int index,
	        ArrayList<Model> models,
	        String verifyModelId,
	        String property,
	        ExecutorService executor) throws IOException {

	    if (index >= rounds.size()) {
	        Platform.runLater(() -> progressIndicator.setVisible(false));
	        executor.shutdown();
	        return;
	    }

	    HashMap<Model, HashMap<String, Double>> round = rounds.get(index);

	    // Apply parameter values and write to files
	    for (Model m : round.keySet()) {
	        HashMap<String, Double> parameters = round.get(m);
	        for (Map.Entry<String, Double> entry : parameters.entrySet()) {
	            if (entry.getValue() != null) {
	                m.getExternalParameter(entry.getKey()).setValue(entry.getValue());
	            }
	        }
	        FileUtils.writeParametersToFile(m.getVerificationFilePath(), parameters);
	    }

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
	                    verifyResults.appendText(
	                        "Result for model: {" + verifyModelId + "} with property: {" + property + "}\nResult: " + result + "\n"
	                    );
	                } else {
	                    verifyResults.appendText(
	                        "Verification failed for model: {" + verifyModelId + "} with property: {" + property + "}\n"
	                    );
	                    Alerter.showErrorAlert("Verification Failed", "Check the logs for the reason of failure");
	                }
	            });

	            // Proceed to next round
	            try {
					runVerificationsSequentially(rounds, index + 1, models, verifyModelId, property, executor);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        });
	}

}
