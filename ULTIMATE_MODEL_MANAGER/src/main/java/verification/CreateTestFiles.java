package verification;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Model;
import project.Project;
import sharedContext.SharedContext;

public class CreateTestFiles {
	
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();
	private static final Logger logger = LoggerFactory.getLogger(CreateTestFiles.class);
	
	public static void createTestFiles(List<Model> models) {
		
	}

}
