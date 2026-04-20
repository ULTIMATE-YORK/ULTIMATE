package sharedContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mariuszgromada.math.mxparser.mXparser;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import project.Project;
import ultimate.Ultimate;
import verification.NPMCVerification;
import org.mariuszgromada.math.mxparser.License;
import utils.Alerter;

public class SharedContext {

    private static final SharedContext context = new SharedContext(); // Singleton instance
    private static Ultimate ultimate;
    private static Project project;
    private static Stage mainStage;
    private static final List<Stage> openSecondaryStages = new ArrayList<>();

    private SharedContext() {
        ultimate = new Ultimate();
        License.iConfirmNonCommercialUse("ULTIMATE");
    }

    public static SharedContext getContext() {
        return context;
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void setMainStage(Stage _mainStage) {
        mainStage = _mainStage;
    }

    public static Ultimate getUltimateInstance() {
        return ultimate;
    }

    public static Project getProject() {
        return project;
    }

    public static void setProject(Project _project) {
        project = _project;
    }

    public static void setUltimate(Ultimate _ultimate) {
        ultimate = _ultimate;
    }

    public static void registerSecondaryStage(Stage stage) {
        openSecondaryStages.add(stage);
        stage.setOnHidden(e -> openSecondaryStages.remove(stage));
    }

    public static void closeAllSecondaryStages() {
        new ArrayList<>(openSecondaryStages).forEach(Stage::close);
        openSecondaryStages.clear();
        ui.Plotting.killAllPlotProcesses();
    }

    public static void reset(Project newProject) {
        closeAllSecondaryStages();
        if (!newProject.isConfigured()) {
            if (mainStage != null) {
                mainStage.close();
            }
            return;
        }
        NPMCVerification.clearCache();
        ultimate = new Ultimate();
        String previousChosenPMC = project != null ? project.getChosenPMC() : null;
        project = newProject;
        if (previousChosenPMC != null) {
            newProject.setChosenPMC(previousChosenPMC);
        }

        FXMLLoader loader = new FXMLLoader(SharedContext.class.getResource("/view/main_view.fxml"));
        try {
            GridPane root = loader.load();
            if (mainStage != null && mainStage.getScene() == null) {
                // First launch: size to 80% of screen and centre
                Rectangle2D screen = Screen.getPrimary().getVisualBounds();
                double w = screen.getWidth() * 0.8;
                double h = screen.getHeight() * 0.8;
                mainStage.setScene(new Scene(root, w, h));
                mainStage.setMinWidth(800);
                mainStage.setMinHeight(600);
                mainStage.setX(screen.getMinX() + (screen.getWidth() - w) / 2);
                mainStage.setY(screen.getMinY() + (screen.getHeight() - h) / 2);
                mainStage.getIcons()
                        .add(new Image(SharedContext.class.getResourceAsStream("/images/ultimate_logo_256x256.png")));
                mainStage.show();
            } else if (mainStage != null) {
                // Subsequent load: keep stage size/position, just swap scene root
                mainStage.getScene().setRoot(root);
            }
            String title = "ULTIMATE - Stochastic World Model Verification & Synthesis: "
                    + newProject.getProjectName();
            mainStage.setTitle(newProject.isModified() ? title + " *" : title);
            mainStage.setOnCloseRequest(event -> {
                if (newProject.isModified()) {
                    boolean userConfirms = utils.Alerter.showConfirmationAlert("Unsaved Changes!",
                            "You have unsaved changes. Do you want to close without saving?");
                    if (!userConfirms) {
                        event.consume();
                        return;
                    }
                }
                closeAllSecondaryStages();
                Platform.exit();
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not load the GridPane resource at /view/main_view.fxml");
        }
    }

}
