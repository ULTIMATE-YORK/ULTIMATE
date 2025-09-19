package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.SynthesisRun;
import data.VerificationResult;
import data.VerificationRun;
import evochecker.exception.EvoCheckerException;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Model;
import parameters.SynthesisGoal;
import project.Project;
import property.Property;
// import results.RangedExperimentResults;
import sharedContext.SharedContext;
import ultimate.Ultimate;
import utils.Alerter;
import utils.DialogOpener;
import utils.Font;

public class PropertiesController {

	@FXML
	private Button addPropertyButton;
	@FXML
	private Button addSynthesisObjectiveButton;
	@FXML
	private Button removeSynthesisObjectiveButton;
	@FXML
	private Button removePropertyButton;
	@FXML
	private Button scrollUp;
	@FXML
	private Button scrollDown;
	@FXML
	private Button verifyButton;
	// @FXML
	// private ListView<String> verifyResults;
	@FXML
	private ListView<SynthesisRun> synthesisRunsView;
	@FXML
	private ListView<VerificationRun> verificationRunsView;
	// @FXML private ProgressIndicator progressIndicator;
	@FXML
	private ListView<Property> propertyListView;
	@FXML
	private ListView<SynthesisGoal> synthesisListView;
	@FXML
	private ChoiceBox<String> xaxisparam;
	@FXML
	private ProgressIndicator progressIndicatorSynthesis;
	@FXML
	private ProgressIndicator progressIndicatorVerification;

	private Label modalLabel;
	private String currentModelId = null;
	private String currentProperty = null;

	private Ultimate ultimate;

	private ObservableList<String> allVerificationDisplayResults = FXCollections.observableArrayList();
	private ObservableList<SynthesisRun> synthesisRuns = FXCollections.observableArrayList();
	private ObservableList<VerificationRun> verificationRuns = FXCollections.observableArrayList();
	private FilteredList<String> filteredVerificationResults = new FilteredList<>(allVerificationDisplayResults,
			s -> true);

	private Project project = SharedContext.getProject();

	private String xaxis = "";

	private ProgressIndicator modalProgress;

	@FXML
	private void initialize() {
		
		addPropertyButton.disableProperty().bind(new SimpleBooleanProperty(project.getTargetModel() == null));
		addSynthesisObjectiveButton.disableProperty().bind(new SimpleBooleanProperty(project.getTargetModel() == null));

		removePropertyButton.disableProperty().bind(Bindings.isNull(propertyListView.getSelectionModel().selectedItemProperty()));
		removeSynthesisObjectiveButton.disableProperty().bind(Bindings.isNull(synthesisListView.getSelectionModel().selectedItemProperty()));

		if (project.getTargetModel() != null) {

			propertyListView.setItems(project.getTargetModel().getProperties());
			synthesisListView.setItems(project.getTargetModel().getSynthesisGoals());
//			synthesisListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		}


		
		// verifyResults.setItems(filteredVerificationResults);
		synthesisRunsView.setItems(synthesisRuns);
		verificationRunsView.setItems(verificationRuns);
		synthesisRunsView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				SynthesisRun selectedRun = synthesisRunsView.getSelectionModel().getSelectedItem();
				if (selectedRun != null) {
					try {
						FXMLLoader loader = new FXMLLoader(
								getClass().getResource("/dialogs/synthesis_run_dialog.fxml"));
						Parent root = loader.load();

						SynthesisRunBoxController controller = loader.getController();
						Stage stage = new Stage();

						controller.setSynthesisRun(selectedRun);
						controller.setStage(stage);

						stage.setTitle("Synthesis Run " + selectedRun.getRunId());
						stage.initModality(Modality.APPLICATION_MODAL);
						stage.setScene(new Scene(root));
						stage.showAndWait();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		verificationRunsView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				VerificationRun selectedRun = verificationRunsView.getSelectionModel().getSelectedItem();
				if (selectedRun != null) {
					try {
						FXMLLoader loader = new FXMLLoader(
								getClass().getResource("/dialogs/verification_run_dialog.fxml"));
						Parent root = loader.load();

						VerificationRunBoxController controller = loader.getController();
						Stage stage = new Stage();

						controller.setVerificationRun(selectedRun);
						controller.setStage(stage);

						stage.setTitle("Verification Run " + selectedRun.getRunId());
						stage.initModality(Modality.APPLICATION_MODAL);
						stage.setScene(new Scene(root));
						stage.showAndWait();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		// UiUtilities.makeListViewTextSelectable(verifyResults);
		setCells();
		setListeners();
	}

	@FXML
	private void addProperty() throws IOException {
		if (project.getTargetModel() == null) {
			Alerter.showErrorAlert("No Model Selected", "Select a model to which to add a property!");
			return;
		}
		DialogOpener.openDialogWindow(SharedContext.getMainStage(), "/dialogs/add_property.fxml", "Add Property");
	}

	@FXML
	private void removeProperty() throws IOException {
		Property p = propertyListView.getSelectionModel().getSelectedItem();
		project.getTargetModel().removeProperty(p);
	}
	
	@FXML
	private void addSynthesisObjective() throws IOException {
		if (project.getTargetModel() == null) {
			Alerter.showErrorAlert("No Model Selected", "Select a model to which to add a synthesis objective!");
			return;
		}
		DialogOpener.openDialogWindow(SharedContext.getMainStage(), "/dialogs/add_synthesis_objective.fxml",
				"Add Synthesis Objective");
	}

	@FXML
	private void removeSynthesisObjective() throws IOException {
		if (project.getTargetModel() == null) {
			Alerter.showErrorAlert("No Model Selected", "Select a model to which to add a synthesis objective!");
			return;
		}
		SynthesisGoal so = synthesisListView.getSelectionModel().getSelectedItem();
		project.getTargetModel().removeSynthesisObjective(so);
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
	private void exportResults() {
		String projectDirectory = project.getDirectoryPath();
		String verificationText = ""; // verifyResults.getSelectionModel().getSelectedItem();
		String fileName = "";
		try {
			BufferedReader reader = new BufferedReader(new StringReader(verificationText));

			// Extract model and property
			String headerLine = reader.readLine();
			if (headerLine == null || !headerLine.startsWith("Verification of")) {
				return;
			}

			String[] headerParts = headerLine.substring("Verification of ".length()).split(" with property: ");
			if (headerParts.length != 2) {
				return;
			}

			String modelName = headerParts[0].trim().replaceAll("\\s+", "-");
			String property = headerParts[1].trim().replaceAll("[^a-zA-Z0-9=\\[\\]_-]", "");
			fileName = modelName + "_" + property + "_results.csv";

			// Create results directory if it doesn't exist
			File resultsDir = new File(projectDirectory, "results");
			if (!resultsDir.exists()) {
				resultsDir.mkdirs();
			}

			File outputFile = new File(resultsDir, fileName);
			FileWriter writer = new FileWriter(outputFile);

			// Parse all configurations
			List<String> parameterNames = new ArrayList<>();
			List<List<String>> rows = new ArrayList<>();

			String line;
			List<String> currentRow = new ArrayList<>();
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				if (line.startsWith("Configuration:")) {
					currentRow = new ArrayList<>();
				} else if (line.startsWith("Result:")) {
					String result = line.substring("Result:".length()).trim();
					currentRow.add(result);
					rows.add(currentRow);
				} else if (line.contains(":")) {
					String[] parts = line.split(":", 2);
					if (parts.length == 2) {
						String param = parts[0].trim();
						String value = parts[1].trim();
						currentRow.add(value);
						if (!parameterNames.contains(param)) {
							parameterNames.add(param);
						}
					}
				}
			}

			// Write header
			for (String param : parameterNames) {
				writer.append(param).append(",");
			}
			writer.append("result\n");

			// Write rows
			for (List<String> row : rows) {
				for (int i = 0; i < row.size(); i++) {
					writer.append(row.get(i));
					if (i < row.size() - 1)
						writer.append(",");
				}
				writer.append("\n");
			}

			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		Alerter.showInfoAlert("Export Successful",
				"Results have been exported to: " + projectDirectory + "/results/" + fileName);
	}

	@FXML
	private void verify() throws IOException {

		Model vModel = project.getTargetModel();
		Property vProp = propertyListView.getSelectionModel().getSelectedItem();

		if (!validateSelection(vModel, vProp))
			return;

		if (project.containsInternalParameters()) {
			Platform.runLater(() -> {
				Alerter.showInfoAlert("Cannot Verify",
						"Cannot verify this property because the model contains internal parameters."
								+ "Please modify these to be external or dependency parameters and try again.");
			});
			return;
		}

		Stage modalStage = createPopUpStage("Verification in Progress",
				"Verifying property " + vProp.getDefinition() + " of model " + vModel.getModelId());
		modalStage.show();
		modalProgress.setVisible(true);

		CompletableFuture.runAsync(() -> {
			try {
				progressIndicatorVerification.setVisible(true);
				if (project.containsRangedParameters()) {
					handleRangedVerification(vModel, vProp, modalStage);
				} else {
					handleSimpleVerification(vModel, vProp, modalStage);
				}
			} catch (Exception e) {
				e.printStackTrace();
				showVerificationError(modalStage);
			} finally {
				progressIndicatorVerification.setVisible(false);
			}
		});
	}

	@FXML
	private void plotResults() {
		String selectedResult = ""; // verifyResults.getSelectionModel().getSelectedItem();
		if (selectedResult == null || selectedResult.isEmpty())
			return;

		ParsedVerificationData parsedData = parseVerificationResult(selectedResult);

		if (parsedData.configMaps.isEmpty() || parsedData.resultValues.isEmpty())
			return;

		// Handle single-parameter plot
		if (parsedData.configMaps.get(0).size() == 1) {
			plotSingleParameter(parsedData);
		} else {
			plotMultiParameter(parsedData);
		}
	}

	// @FXML
	// private void confirmPlot() {
	// String selectedXaxis = xaxisparam.getSelectionModel().getSelectedItem();
	// if (selectedXaxis == null || selectedXaxis.isEmpty()) {
	// Alerter.showErrorAlert("Invalid Selection", "Please choose a parameter for
	// the X-axis.");
	// return;
	// }

	// xaxis = selectedXaxis;

	// // Close the dialog window
	// Stage stage = (Stage) confirmPlotButton.getScene().getWindow();
	// stage.close();
	// }

	// @FXML
	// private void cancelPlot() {
	// xaxis = "";
	// Stage stage = (Stage) cancelPlotButton.getScene().getWindow();
	// stage.close();
	// }

	private boolean validateSelection(Model vModel, Property vProp) {
		if (vModel == null || vProp == null) {
			Alerter.showErrorAlert("CANNOT VERIFY", "Please select a model and a property to run verification on");
			return false;
		}
		return true;
	}

	private Stage createPopUpStage(String title, String labelContents) {
		Stage modalStage = new Stage();
		modalStage.initModality(Modality.APPLICATION_MODAL);
		modalStage.setTitle(title);

		modalProgress = new ProgressIndicator();
		modalProgress.setPrefSize(400, 400);

		modalLabel = new Label(labelContents);

		VBox box = new VBox(20, modalProgress, modalLabel);
		box.setAlignment(Pos.CENTER);

		Scene modalScene = new Scene(box, 300, 200);
		modalStage.setScene(modalScene);

		return modalStage;
	}

	private void showVerificationError(Stage modalStage) {
		Platform.runLater(() -> {
			modalStage.close();
			Alerter.showErrorAlert("Verification Failed",
					"There was an error communicating with the verification engine. Please run verification again.");
		});
	}

	private void handleRangedVerification(Model vModel, Property vProp, Stage modalStage) throws Exception {

		ArrayList<HashMap<String, String>> experimentPlan = project.generateExperimentPlan();
		ExecutorService executor = Executors.newSingleThreadExecutor();

		ultimate = SharedContext.getUltimateInstance();
		ultimate.loadModelsFromProject();
		ultimate.setTargetModelById(currentModelId);

		String runId = "Ver_" + UUID.randomUUID().toString() + "_" + vModel.getModelId();
		VerificationRun run = new VerificationRun(runId, currentModelId, vProp.getDefinition(), false);
		ultimate.setVerificationProperty(vProp.getDefinition());
		runSequentialRangedVerifications(0, vModel, vProp, experimentPlan, run, executor, modalStage);

	}

	private void runSequentialRangedVerifications(int index, Model vModel, Property vProp,
			ArrayList<HashMap<String, String>> experimentPlan, VerificationRun run,
			ExecutorService executor, Stage modalStage)
			throws IOException, Exception {

		String cacheKey = project.generateVerificationCacheKey(vModel, vProp);

		if (index == experimentPlan.size()) {
			Platform.runLater(() -> {
				verificationRuns.add(run);
				modalStage.close();
			});
			return;
		}

		CompletableFuture
				.supplyAsync(() -> {
					HashMap<String, String> thisIterationExternalParameterValues = experimentPlan.get(index);
					for (Model m : project.getModels()) {
						m.setExternalParametersByUniqueIdMap(thisIterationExternalParameterValues);
					}

					if (project.getCacheResult(cacheKey) != null) {
						run.setRetrievedFromCache(true);
						VerificationResult result = new VerificationResult(currentModelId, vProp.getDefinition(),
								project.getCacheResult(cacheKey));
						return result;
					} else {
						try {
							VerificationResult result = ultimate.executeVerification();
							return result;
						} catch (IOException e) {
							e.printStackTrace();
							return null;
						}
					}
				}, executor)
				.thenAccept(result -> {
					try {
						if (result != null) {
							project.addCacheResult(cacheKey,
									((VerificationResult) result).getVerifiedPropertyValueMap());
							run.addResult((VerificationResult) result);
						} else {
							throw new Exception("No results from ULTIMATE!");
						}
					} catch (Exception e) {
						e.printStackTrace();
						modalStage.close();
						Platform.runLater(() -> {
							modalStage.close();
							Alerter.showErrorAlert("Verification Failed",
									"There was an error communicating with the verification engine. Please run verification again.");
						});
					}

					Platform.runLater(() -> {
						modalProgress.setProgress((((double) index) + 1) / ((double) experimentPlan.size()));
						modalLabel.setText(String.format("%d/%d complete...", index + 1, experimentPlan.size()));
					});

					try {
						runSequentialRangedVerifications(index + 1, vModel, vProp, experimentPlan, run, executor,
								modalStage);
					} catch (Exception e) {
						e.printStackTrace();
						modalStage.close();
						Alerter.showErrorAlert("Verification Failed",
								"There was an error communicating with the verification engine. Please run verification again.");
					}
				})
				.exceptionally(ex -> {
					ex.printStackTrace();
					Platform.runLater(() -> {
						modalStage.close(); // Ensure modal is closed on error
						Alerter.showErrorAlert("Verification Failed",
								"There was an error communicating with the verification engine. Please run verification again");
					});
					return null;
				});

	}

	private void handleSimpleVerification(Model vModel, Property vProp, Stage modalStage) throws IOException {

		String cacheKey = project.generateVerificationCacheKey(vModel, vProp);

		if (project.getCacheResult(cacheKey) != null) {

			String runId = UUID.randomUUID().toString();
			VerificationRun run = new VerificationRun(runId, vModel.getModelId(), vProp.getDefinition(), true);
			VerificationResult vr = new VerificationResult(run, project.getCacheResult(cacheKey));
			run.addResult(vr);
			verificationRuns.add(run);

			Platform.runLater(modalStage::close);
			return;
		}

		Ultimate ultimate = SharedContext.getUltimateInstance();
		ultimate.loadModelsFromProject();
		ultimate.setTargetModelById(vModel.getModelId());
		ultimate.setVerificationProperty(vProp);
		ultimate.executeVerification();
		HashMap<String, String> result = ultimate.getVerificationResultsMap();
		String runId = UUID.randomUUID().toString();
		VerificationRun run = new VerificationRun(runId, vModel.getModelId(), vProp.getDefinition(), false);
		VerificationResult vr = new VerificationResult(run, result);
		run.addResult(vr);
		project.addCacheResult(cacheKey, result);
		verificationRuns.add(run);

		Platform.runLater(() -> {
			modalStage.close();
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
					} catch (NumberFormatException ignored) {
					}
				}
				inConfigSection = false;
			} else if (inConfigSection && currentConfig != null && line.contains(":")) {
				String[] parts = line.split(":", 2);
				if (parts.length == 2) {
					try {
						currentConfig.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
					} catch (NumberFormatException ignored) {
					}
				}
			}
		}

		return new ParsedVerificationData(configMaps, resultValues);
	}

	private void plotSingleParameter(ParsedVerificationData data) {
		String configKey = data.configMaps.get(0).keySet().iterator().next();

		// Compute min and max x-values
		double minX = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;

		for (HashMap<String, Double> config : data.configMaps) {
			double x = config.get(configKey);
			minX = Math.min(minX, x);
			maxX = Math.max(maxX, x);
		}

		// Add a small padding for nicer appearance
		double padding = (maxX - minX) * 0.05;
		if (padding == 0)
			padding = 1; // Prevent zero padding if all values are the same

		NumberAxis xAxis = new NumberAxis(minX - padding, maxX + padding, (maxX - minX) / 10);
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
			if (result == null || !config.containsKey(xaxis))
				continue;

			double xVal = config.get(xaxis);
			StringBuilder labelBuilder = new StringBuilder();

			for (Map.Entry<String, Double> entry : config.entrySet()) {
				if (!entry.getKey().equals(xaxis)) {
					if (labelBuilder.length() > 0)
						labelBuilder.append(", ");
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

		// ➤ Combine all series into a single chart
		// Compute min and max x values
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;

		for (HashMap<String, Double> config : data.configMaps) {
			if (config.containsKey(xaxis)) {
				double x = config.get(xaxis);
				if (x < minX)
					minX = x;
				if (x > maxX)
					maxX = x;
			}
		}

		// Create axis with custom bounds
		NumberAxis xAxisObj = new NumberAxis(minX, maxX, (maxX - minX) / 10.0);
		xAxisObj.setLabel(xaxis);
		NumberAxis yAxisObj = new NumberAxis();
		yAxisObj.setLabel("Result");

		LineChart<Number, Number> lineChart = new LineChart<>(xAxisObj, yAxisObj);
		lineChart.setTitle(currentModelId + " - " + currentProperty);

		// ➤ Add all series to the chart
		for (XYChart.Series<Number, Number> s : seriesMap.values()) {
			lineChart.getData().add(s);
		}

		// ➤ Show single chart with all series
		showChart(lineChart, "Plot of Results");
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
				Alerter.showInfoAlert("Old/New model", oldModel.getModelId() + "/" + newModel.getModelId());	
				if (newModel != null) {
					propertyListView.setItems(newModel.getProperties());
					synthesisListView.setItems(newModel.getSynthesisGoals());
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
					currentProperty = newProperty.getDefinition();
				} else {
					currentProperty = null;
				}
				updateVerifyFilter(); // <- apply updated filtering
			});
		});

		// verifyResults.getSelectionModel().selectedItemProperty().addListener((obs,
		// oldVal, newVal) -> {
		// plotButton.setVisible(hasMultipleResults(newVal));
		// });

		// // Add checkbox listener
		// showAllResults.selectedProperty().addListener((obs, wasSelected, isSelected)
		// -> {
		// updateVerifyFilter();
		// });

		// verifyResults.getSelectionModel().selectedItemProperty().addListener((obs,
		// oldVal, newVal) -> {
		// exportButton.setVisible(hasMultipleResults(newVal));
		// });

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
					Label label = new Label(item.getDefinition()); // Display the model ID
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
					project.getTargetModel().removeProperty(selectedProperty); // Remove the selected property
				}
			}
		});

		// verifyResults.setCellFactory(null);
	}

	private void addVerificationResult(String result) {
		Platform.runLater(() -> {
			allVerificationDisplayResults.add(result); // <-- add to backing list
		});
	}

	private void updateVerifyFilter() {
		// if (showAllResults != null && showAllResults.isSelected()) {
		// // Show all results
		// filteredVerificationResults.setPredicate(s -> true);
		// } else {
		// // Filter by current model and property
		// filteredVerificationResults.setPredicate(result -> {
		// boolean matchesModel = (currentModelId == null ||
		// result.contains(currentModelId));
		// boolean matchesProperty = (currentProperty == null ||
		// result.contains(currentProperty));
		// return matchesModel && matchesProperty;
		// });
		// }
	}

	// TODO: clean this up
	private boolean hasMultipleResults(String text) {
		if (text == null || text.isEmpty())
			return false;

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

	private void setPopUpContents(String text) {
		Platform.runLater(() -> {
			modalLabel.setText(text);
		});
	}

	private void appendPopUpContents(String text) {
		Platform.runLater(() -> {
			modalLabel.setText(modalLabel.getText() + "\n" + text);
		});
	}

	@FXML
	public void synthesise() {

		Ultimate ultimate = SharedContext.getUltimateInstance();
		progressIndicatorSynthesis.setVisible(true);

		if (SharedContext.getProject().getAllInternalParameters().size() == 0) {
			Platform.runLater(() -> {
				Alerter.showInfoAlert("Cannot Run Synthesis",
						"Cannot run synthesis for this world model as there are no internal parameters in the constituent models. Please add some and try again.");
			});
			return;
		}

		if (SharedContext.getProject().getAllSynthesisObjectives().size() == 0) {
			Platform.runLater(() -> {
				Alerter.showInfoAlert("Cannot Run Synthesis",
						"Cannot run synthesis for this world model as there are no synthesis objectives in the constituent models. Please add some and try again.");
			});
			return;
		}

		Stage modalStage = createPopUpStage("Synthesis in Progress",
				"Running synthesis for " + project.getProjectName());

		String runId = "Synth_" + UUID.randomUUID().toString() + "_" + project.getProjectName();

		Task<Void> task = new Task<>() {

			@Override
			protected Void call() throws Exception {

				String message = "Initialising...";
				ultimate.loadModelsFromProject();
				ultimate.initialiseSynthesis();
				SynthesisRun run = ultimate.executeSynthesis(runId);
					
				Platform.runLater(() -> {
					modalStage.close();
					progressIndicatorSynthesis.setVisible(false);
					synthesisRuns.add(run);
				});

				return null;
			}

		};

		task.setOnFailed(event -> {
			Throwable e = task.getException();
			e.printStackTrace();
			modalStage.close();
			Platform.runLater(() -> {
				Alerter.showErrorAlert("Synthesis Error", "An exception occured during synthesis:\n" + e.getMessage());
				progressIndicatorSynthesis.setVisible(false);
				appendPopUpContents("An error occurred during synthesis.\n" + e);
				modalProgress.setVisible(false);
			});
		});

		new Thread(task).start();

		modalLabel.textProperty()
				.set("Running synthesis. This may take some time.\nCheck the console window to see progress.");
		modalProgress.setVisible(true);
		modalStage.show();

	}

}
