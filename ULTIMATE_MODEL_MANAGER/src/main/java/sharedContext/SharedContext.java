package sharedContext;

import java.io.IOException;

import org.mariuszgromada.math.mxparser.mXparser;

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

    public static void setUltimate(Ultimate _ultimate){
        ultimate = _ultimate;
    }

    public static void loadProjectFromPath(String projectPath) {
        try {
            project = new Project(projectPath);
            // System.out.
        } catch (IOException e) {
            System.err.println("Error: encountered an exception whilst loading the project from " + projectPath);
            e.printStackTrace();
        }
    }

}