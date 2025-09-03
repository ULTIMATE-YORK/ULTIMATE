package controllers;

import data.SynthesisRun;
import data.SynthesisSolution;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import ui.UiUtilities;

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
    public void plot() {

    }

    @FXML
    public void export() {

    }

}
