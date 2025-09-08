package controllers;

import java.io.IOException;

import data.SynthesisRun;
import data.SynthesisSolution;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import synthesis.SynthesisExport;
import ui.Plotting;
import ui.UiUtilities;
import utils.Alerter;
import utils.DialogOpener;

public class SynthesisRunBoxController {

    @FXML
    ListView<SynthesisSolution> solutions;
    @FXML
    ListView<String> details;

    private SynthesisRun run;
    private Stage stage;

    @FXML
    public void initialize() {

        UiUtilities.makeListViewTextSelectable(solutions);
        UiUtilities.makeListViewTextSelectable(details);

    }

    public void setSynthesisRun(SynthesisRun run) {
        this.run = run;
        solutions.setItems(run.getSolutions());
        details.setItems(run.getDetails());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void close() {
        stage.close();
    }

    @FXML
    public void plotFront() {
        System.out.println("Plotting front...");
        if (run.getAllSynthesisObjectiveDefinitions().size() < 2
                || run.getAllSynthesisObjectiveDefinitions().size() > 3) {
            Platform.runLater(() -> {
                Alerter.showErrorAlert("Bad Number of Variables",
                        "Could not plot: plotting the Pareto front is only possible for runs with 2 or 3 synthesis objectives.");
            });
        }
        try {
            Plotting.plotParetoFront(run);
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alerter.showErrorAlert("Plotting Error", "ERROR: Could not start the plotting script.");
            });
        }
    }

    @FXML
    public void plotSet() {
        System.out.println("Plotting set...");
        if (run.getAllInternalParameterNames().size() < 2
                || run.getAllInternalParameterNames().size() > 3) {
            Platform.runLater(() -> {
                Alerter.showErrorAlert("Bad Number of Variables",
                        "Could not plot: plotting the synthesised values is only possible for runs with 2 or 3 internal parameters.");
            });
        }
        try {
            Plotting.plotParetoSet(run);
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alerter.showErrorAlert("Plotting Error", "ERROR: Could not start the plotting script.");
            });
        }
    }

  @FXML
    public void exportSet() {

        String exportPath = DialogOpener.openDataSaveDialog(stage, run.getRunId() + "_set");
        try {
            SynthesisExport.createPermanentCopy(run.getParetoSetFilePath(), exportPath);
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alerter.showErrorAlert("Saving Error",
                        "ERROR: Could not save the data file. Do you have permission to save in that location?");
            });
        }

    }

    @FXML
    public void exportFront() {

        String exportPath = DialogOpener.openDataSaveDialog(stage, run.getRunId() + "_front");
        try {
            SynthesisExport.createPermanentCopy(run.getParetoFrontFilePath(), exportPath);
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alerter.showErrorAlert("Saving Error",
                        "ERROR: Could not save the data file. Do you have permission to save in that location?");
            });
        }

    }

}
