package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Model;
import parameters.SynthesisObjective;
import project.Project;
import project.synthesis.EvoCheckerUltimateInstance;
import project.synthesis.SynthesisSolution;
import property.Property;
// import results.RangedExperimentResults;
import sharedContext.SharedContext;
import ui.UiUtilities;
import ultimate.Ultimate;
import utils.Alerter;
import utils.DialogOpener;
import utils.Font;
import verification.RangedVerificationResults;

public class PropertiesController {

	@FXML
	private Button addProperty;
	@FXML
	private Button addSynthesisObjective;
	@FXML
	private Button removeSynthesisObjective;
	@FXML
	private Button scrollUp;
	@FXML
	private Button scrollDown;
	@FXML
	private Button verifyButton;
	@FXML
	private ListView<String> verifyResults;
	@FXML
	private ListView<SynthesisSolution> synthesiseResults;
	// @FXML private ProgressIndicator progressIndicator;
	@FXML
	private ListView<Property> propertyListView;
	@FXML
	private ListView<SynthesisObjective> synthesisListView;

	// @FXML
	// private CheckBox showAllResults;
	@FXML
	private CheckBox plotSynthesisCheckBox;
	// @FXML
	// private Button plotButton;
	@FXML
	private ChoiceBox<String> xaxisparam;
	// @FXML
	// private Button confirmPlotButton;
	// @FXML
	// private Button cancelPlotButton;
	// @FXML
	// private Button exportButton;

	private Label modalLabel;
	private String currentModelId = null;
	private String currentProperty = null;

	private Ultimate ultimate;

	private ObservableList<String> allVerificationDisplayResults = FXCollections.observableArrayList();
	private ObservableList<SynthesisSolution> allSynthesisDisplayResults = FXCollections.observableArrayList();
	private FilteredList<String> filteredVerificationResults = new FilteredList<>(allVerificationDisplayResults,
			s -> true);

	private Project project = SharedContext.getProject();

	private String xaxis = "";

	private ProgressIndicator modalProgress;

	@FXML
	private void initialize() {
		if (project.getTargetModel() != null) {

			propertyListView.setItems(project.getTargetModel().getProperties());
			synthesisListView.setItems(project.getTargetModel().getSynthesisObjectives());

			synthesisListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		}
		verifyResults.setItems(filteredVerificationResults); // <--- set filtered list
		synthesiseResults.setItems(allSynthesisDisplayResults);
		UiUtilities.makeListViewTextSelectable(verifyResults);
		UiUtilities.makeListViewTextSelectable(synthesiseResults);
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
		SynthesisObjective so = synthesisListView.getSelectionModel().getSelectedItem();
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
		String verificationText = verifyResults.getSelectionModel().getSelectedItem();
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
		// verificationResult = "";
		// verificationCount = 1;

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

		CompletableFuture.runAsync(() -> {
			try {
				if (project.containsRangedParameters()) {
					handleRangedVerification(vModel, vProp, modalStage);
					// System.err.println("Ranged experiments have been temporarily disabled.");
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

		// System.out.println("experimentPlan: " + experimentPlan);
		ObservableList<RangedVerificationResults> results = FXCollections.observableArrayList();
		ultimate.setVerificationProperty(vProp.getDefinition());
		runVerificationsSequentially2(0, vModel, vProp, experimentPlan, results, executor, modalStage);

	}

	private void runVerificationsSequentially2(int index, Model vModel, Property vProp,
			ArrayList<HashMap<String, String>> experimentPlan, ObservableList<RangedVerificationResults> results,
			ExecutorService executor, Stage modalStage)
			throws IOException, Exception {

		String cacheKey = project.generateCacheKey(vModel, vProp);

		if (index == experimentPlan.size() - 1) {
			Platform.runLater(() -> {
				for (RangedVerificationResults r : results) {
					addVerificationResult(String.format("Experiment result for model '%s':\n%s", vModel.getModelId(),
							r.getDisplayString()));
				}
				modalStage.close();
			});
			return;
		}

		HashMap<String, String> thisIterationExternalParameterValues = experimentPlan.get(index);
		for (Model m : project.getModels()) {
			m.setExternalParametersByUniqueIdMap(thisIterationExternalParameterValues);
		}

		CompletableFuture
				.supplyAsync(() -> {
					if (project.getCacheResult(cacheKey) != null) {
						return project.getCacheResult(cacheKey);
					} else {
						try {
							ultimate.executeVerification();
							return ultimate.getVerificationResults();
						} catch (IOException e) {
							e.printStackTrace();
							return null;
						}
					}
				}, executor)
				.thenAccept(ultimateResults -> {
					Platform.runLater(() -> {
						modalProgress.setProgress((((double) index) + 1) / ((double) experimentPlan.size()));
						modalLabel.setText(String.format("%d/%d complete...", index + 1, experimentPlan.size()));
						try {
							if (ultimateResults != null) {
								project.addCacheResult(cacheKey, ultimateResults);
								results.add(new RangedVerificationResults(
										thisIterationExternalParameterValues, ultimateResults));
							} else {
								modalStage.close(); // Ensure modal is closed on error
								Alerter.showErrorAlert("Verification Failed",
										"There was an error communicating with the verification engine. Please run verification again.");
							}
						} catch (Exception e) {
							e.printStackTrace();
							modalStage.close();
							Alerter.showErrorAlert("Verification Failed",
									"There was an error communicating with the verification engine. Please run verification again.");
						}
					});

					try {
						runVerificationsSequentially2(index + 1, vModel, vProp, experimentPlan, results, executor,
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

		String cacheKey = project.generateCacheKey(vModel, vProp);

		if (project.getCacheResult(cacheKey) != null) {
			String verificationResult = "Verification of " + vModel.getModelId() + " with property: "
					+ vProp.getDefinition()
					+ "\nResult: " + project.getCacheResult(cacheKey).get(vProp.getDefinition()) + "\n";
			addVerificationResult(verificationResult);
			Platform.runLater(modalStage::close);
			return;
		}

		Ultimate ultimate = SharedContext.getUltimateInstance();
		ultimate.loadModelsFromProject();
		ultimate.setTargetModelById(vModel.getModelId());
		ultimate.setVerificationProperty(vProp);
		ultimate.executeVerification();
		HashMap<String, String> result = ultimate.getVerificationResults();
		project.addCacheResult(cacheKey, result);
		String verificationResult = "Verification of " + vModel.getModelId() + " with property: "
				+ vProp.getDefinition()
				+ "\nResult: " + result.get(vProp.getDefinition()) + "\n";
		addVerificationResult(verificationResult);

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
				if (newModel != null) {
					propertyListView.setItems(newModel.getProperties());
					synthesisListView.setItems(newModel.getSynthesisObjectives());
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
	// TODO: this doesn't work if I run it twice in a row
	public void synthesise() {

		Ultimate ultimate = SharedContext.getUltimateInstance();

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
				"Running synthesis for " + currentModelId);
		modalStage.show();

		CompletableFuture.supplyAsync(() -> {
			String runId = LocalDateTime.now()
					.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_" + currentModelId;
			appendPopUpContents("\nInitialising models...");
			ultimate.loadModelsFromProject();
			ultimate.setTargetModelById(currentModelId);
			appendPopUpContents("Generating model files...");
			ultimate.generateEvolvableModelFiles();
			String evolvableProjectFileDir = ultimate.getEvolvableProjectFilePath().toString();
			EvoCheckerUltimateInstance ultimateInstance = new EvoCheckerUltimateInstance(ultimate);
			appendPopUpContents("Initialising EvoChecker...");
			ultimate.createEvoCheckerInstance(ultimateInstance);
			ultimate.initialiseEvoCheckerInstance(evolvableProjectFileDir);
			if (plotSynthesisCheckBox.isSelected()) {
				boolean plottingPossible = 0 < project.getAllSynthesisObjectives().size() &&
						project.getAllSynthesisObjectives().size() < 3 ? true : false;
				ultimate.getEvoCheckerInstance().setParetoFrontPlottingOn(plottingPossible);
			} else {
				System.out.println(
						"Plotting is not possible as the world model has less than 2 or more than 3 synthesis objectives.");
				ultimate.getEvoCheckerInstance().setParetoFrontPlottingOn(false);
			}

			appendPopUpContents("Running synthesis...");
			ultimate.executeSynthesis();
			ArrayList<HashMap<String, String>> synthesisFront = ultimate.getSynthesisParetoFront();
			ArrayList<HashMap<String, String>> synthesisSet = ultimate.getSynthesisParetoSet();

			appendPopUpContents("Synthesis complete.\nCompiling results...");

			ObservableList<SynthesisSolution> runResult = FXCollections.observableArrayList();
			for (int i = 0; i < synthesisFront.size(); i++) {

				HashMap<String, String> internalParameterValues = synthesisSet.get(i);
				HashMap<String, String> objectiveValues = synthesisFront.get(i);

				SynthesisSolution solution = new SynthesisSolution(
						runId,
						Integer.toString(i),
						currentModelId,
						ultimate.getEvoCheckerInstance().getObjectives().stream()
								.map(evochecker.properties.Property::toString).collect(Collectors.toList()),
						ultimate.getEvoCheckerInstance().getConstraints().stream()
								.map(evochecker.properties.Property::toString).collect(Collectors.toList()),
						internalParameterValues,
						objectiveValues);

				runResult.add(solution);
			}
			return runResult;

		}).thenAccept(runResult ->

		{
			allSynthesisDisplayResults.addAll(runResult);
			Platform.runLater(() -> {
				modalStage.close();
				ultimate.writeSynthesisResultsToFile();
			});
		}).exceptionally(e -> {
			e.printStackTrace();

			Platform.runLater(() -> {
				appendPopUpContents("An error occurred during synthesis.\n" + e);
			});

			return null;
		}

		);

	}

}
