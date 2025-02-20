package sharedContext;

import javafx.stage.Stage;
import project.Project;

public class SharedContext {
    
	private static final SharedContext instance = new SharedContext(); // Singleton instance
	private Project project;
    private Stage mainStage;
    
    private SharedContext() {}

    public static SharedContext getInstance() {
        return instance;
    }
    
	public void setProject(Project project) {
		this.project = project;
	}
	
	public Project getProject() {
		return this.project;
	}
	
    public Stage getMainStage() {
        return mainStage;
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }
    

}