package controllers;

import java.io.IOException;

import data.VerificationRun;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import ui.Plotting;
import ui.UiUtilities;
import utils.Alerter;
import utils.DialogOpener;

public class VerificationRunBoxController {

    @FXML
    ListView<String> results;
    @FXML
    ListView<String> details;

    private VerificationRun run;
    private Stage stage;

    private String plotX;
    private String plotY;
    private String plotZ;

    @FXML
    public void initialize() {

        UiUtilities.makeListViewTextSelectable(results);
        UiUtilities.makeListViewTextSelectable(details);

    }

    public void setVerificationRun(VerificationRun run) {
        this.run = run;
        results.setItems(run.getResultStrings());
        details.setItems(run.getDetails());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void close() {
        stage.close();
    }

    public void export() {

        String exportPath = DialogOpener.openDataSaveDialog(stage, run.getModelId() + "_ranged_verification_result");
        try {
            run.ExportToFile(exportPath, false);
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alerter.showErrorAlert("Saving Error",
                        "ERROR: Could not save the verification data file. Do you have permission to save in that location?");
            });
        }

    }

    public void plot() {

        if (run.getAllRangedExternalParameterNames().size() + 1 == 2) {
            plotX = run.getUniqueParameterNames().get(0);
            plotY = run.getPropertyDefinition();
        } else if (run.getUniqueParameterNames().size() + 1 == 3) {
            plotX = run.getUniqueParameterNames().get(0);
            plotY = run.getUniqueParameterNames().get(1);
            plotZ = run.getPropertyDefinition();
        } else {
            Platform.runLater(() -> {
                Alerter.showErrorAlert("Unable to Plot",
                        "Can only produce a plot when there are 2 or 3 variables (ranged parameters plus verification properties)");
            });
        }

        try {
            run.ExportToFile(run.getRunId() + "_tempfile", true);
            Plotting.plotRangedVerification(run, plotX, plotY, plotZ);

        } catch (IOException e) {

            e.printStackTrace();
            Platform.runLater(() -> {
                Alerter.showErrorAlert("Plotting Error",
                        "ERROR: Could not read/write the temporary data file in /tmp/. This is required for plotting. Do you have permission to read/write in that location?");
            });

        }

    }

}
