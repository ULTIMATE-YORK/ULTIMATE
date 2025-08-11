package sharedContext;

import javafx.stage.Stage;
import project.Project;
import ultimate.Ultimate;

public class SharedContext {

    private static final SharedContext context = new SharedContext(); // Singleton instance
    private static Ultimate ultimate;
    private static Stage mainStage;

    private SharedContext() {
        ultimate = new Ultimate();
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

}