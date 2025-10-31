package sharedContext;

import java.io.IOException;

import org.mariuszgromada.math.mxparser.mXparser;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import project.Project;
import ultimate.Ultimate;
import org.mariuszgromada.math.mxparser.License;

public class SharedContext {

    private static final SharedContext context = new SharedContext(); // Singleton instance
    private static Ultimate ultimate;
    private static Project project;
    private static Stage mainStage;

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

    public static void reset(Project newProject) {
        if (!newProject.isConfigured()) {
            if (mainStage != null) {
                mainStage.close();
            }
            return;
        }
        ultimate = new Ultimate();
        project = newProject;

        if (mainStage != null) {
            mainStage.close();
            mainStage = new Stage();

            FXMLLoader loader = new FXMLLoader(mainStage.getClass().getResource("/view/main_view.fxml"));
            try {
                GridPane root = loader.load();
                mainStage.setScene(new Scene(root, 1600, 1000));
                mainStage.setMinWidth(1000);
                mainStage.setMinHeight(800);
                mainStage.setTitle("ULTIMATE");
                mainStage.getIcons()
                        .add(new Image(mainStage.getClass().getResourceAsStream("/images/ultimate_logo_256x256.png")));
                mainStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not load the GridPane resource at /view/main_view.fxml");
            }
        }
    }

}